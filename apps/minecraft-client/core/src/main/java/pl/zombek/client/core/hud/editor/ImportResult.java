package pl.zombek.client.core.hud.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Report containing validation feedback, imported component counts, and unknown/skipped components.
 */
public class ImportResult {
    private final boolean success;
    private final int importedCount;
    private final List<String> importedComponents;
    private final List<String> unknownComponents;
    private final String message;

    public ImportResult(boolean success, int count, List<String> imported, List<String> unknown, String message) {
        this.success = success;
        this.importedCount = count;
        this.importedComponents = imported != null ? imported : new ArrayList<>();
        this.unknownComponents = unknown != null ? unknown : new ArrayList<>();
        this.message = message != null ? message : "";
    }

    public boolean isSuccess() {
        return success;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public List<String> getImportedComponents() {
        return Collections.unmodifiableList(importedComponents);
    }

    public List<String> getUnknownComponents() {
        return Collections.unmodifiableList(unknownComponents);
    }

    public String getMessage() {
        return message;
    }
}
