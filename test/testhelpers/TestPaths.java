package testhelpers;

import java.io.File;

public final class TestPaths {
    private TestPaths() {
    }

    public static File repoFile(String relativePath) {
        File direct = new File(relativePath);
        if (direct.exists()) {
            return direct;
        }
        File fromParent = new File(".." + File.separator + relativePath);
        if (fromParent.exists()) {
            return fromParent;
        }
        return direct;
    }
}
