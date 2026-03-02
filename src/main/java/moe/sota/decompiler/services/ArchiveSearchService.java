package moe.sota.decompiler.services;

import lombok.experimental.UtilityClass;
import moe.sota.decompiler.controllers.TabsController;
import moe.sota.decompiler.models.ArchiveModel;
import moe.sota.decompiler.models.BaseModel;
import moe.sota.decompiler.models.FileModel;
import moe.sota.decompiler.types.ClassType;
import moe.sota.decompiler.types.ImageType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@UtilityClass
public class ArchiveSearchService {

    public record SearchMatch(FileModel fileModel, int lineNumber, String lineContent) {}

    public CompletableFuture<List<SearchMatch>> searchAsync(
            ArchiveModel archive,
            String query,
            boolean caseSensitive,
            Consumer<Integer> progressCallback) {

        return CompletableFuture.supplyAsync(() -> {
            List<FileModel> files = collectFiles(archive);
            List<SearchMatch> results = new ArrayList<>();
            String needle = caseSensitive ? query : query.toLowerCase();

            for (int i = 0; i < files.size(); i++) {
                FileModel file = files.get(i);
                progressCallback.accept(i * 100 / files.size());
                try {
                    String content = getContent(file);
                    if (content == null) continue;
                    String[] lines = content.split("\n", -1);
                    for (int ln = 0; ln < lines.length; ln++) {
                        String haystack = caseSensitive ? lines[ln] : lines[ln].toLowerCase();
                        if (haystack.contains(needle))
                            results.add(new SearchMatch(file, ln + 1, lines[ln].strip()));
                    }
                } catch (Exception ignored) {
                }
            }

            progressCallback.accept(100);
            return results;
        });
    }

    private String getContent(FileModel fileModel) throws Exception {
        if (fileModel.getType() instanceof ImageType) return null;
        if (fileModel.getType() instanceof ClassType)
            return TabsController.getINSTANCE().getTransformer().newInstance().transform(fileModel);
        return new String(fileModel.getBytes(), StandardCharsets.UTF_8);
    }

    private List<FileModel> collectFiles(BaseModel model) {
        List<FileModel> result = new ArrayList<>();
        for (BaseModel child : model.getChildren()) {
            if (child instanceof FileModel)
                result.add((FileModel) child);
            else
                result.addAll(collectFiles(child));
        }
        return result;
    }

}
