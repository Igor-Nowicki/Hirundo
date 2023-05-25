package mockups;

import com.hirundo.libs.services.IBirdRecordDataLoaderBuilder;
import com.hirundo.libs.services.IFileDataLoader;

public class MockBirdRecordDataLoaderBuilder implements IBirdRecordDataLoaderBuilder {
    public String Filename;
    public String TableName;
    public IFileDataLoader FileDataLoader;

    @Override
    public IBirdRecordDataLoaderBuilder withFilename(String filename) {
        Filename = filename;
        return this;
    }

    @Override
    public IBirdRecordDataLoaderBuilder withTableName(String tableName) {
        TableName = tableName;
        return this;
    }

    @Override
    public IFileDataLoader build() {
        return FileDataLoader;
    }
}
