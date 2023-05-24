package mockups;

import com.hirundo.libs.data_structures.NewDbBirdRecord;
import com.hirundo.libs.services.IBirdRecordDataLoader;

import java.util.ArrayList;
import java.util.List;

public class MockNewDbBirdRecordDataLoader implements IBirdRecordDataLoader<NewDbBirdRecord> {
    public List<NewDbBirdRecord> Data = new ArrayList<>();
    public boolean IsLoaded;
    public String FileName;
    public String TableName;

    @Override
    public List<NewDbBirdRecord> loadData(String tableName) {
        IsLoaded = true;
        TableName = tableName;
        return Data;
    }

    @Override
    public void setFileName(String fileName) {
        FileName = fileName;
    }
}

