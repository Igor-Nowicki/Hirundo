package com.hirundo.app.controllers;

import com.hirundo.app.controllers.mockups.MockCsvFileSaver;
import com.hirundo.libs.data_structures.*;
import com.hirundo.mockups.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainControllerTests {
    MainController controller;
    MockBirdRecordDataLoaderBuilder builder;
    MockFileChooser fileChooser;
    MockFileDataLoader fileDataLoader;

    @BeforeEach
    void setUp() {
        fileDataLoader = new MockFileDataLoader();
        builder = new MockBirdRecordDataLoaderBuilder();
        builder.FileDataLoader = fileDataLoader;
        fileChooser = new MockFileChooser();
        controller = new MainController(builder, fileChooser);
    }

    @Test
    void loadData() throws Exception {
        fileDataLoader.IsLoaded = false;

        controller.loadData();

        assertTrue(fileDataLoader.IsLoaded);
    }

    @Test
    void selectFileName() {
        fileChooser.FileName = "file.mdb";

        final String selectedFileName = controller.selectFileName();

        assertEquals("file.mdb", selectedFileName);
        assertEquals("file.mdb", controller.getSelectedFileName());
    }

    @Test
    void getSingleSpecies() throws Exception {
        var record1 = new NewDbBirdRecord();
        record1.SpeciesCode = "AAA.BBB";
        record1.EnglishName = "Aaabin Bbbir";
        record1.LatinName = "Aaarus Bbbirix";

        var record2 = new OldDbBirdRecord();
        record2.Spec = "AAA.BBB";

        fileDataLoader.Data = List.of(DbBirdRecord.from(record2), DbBirdRecord.from(record1));

        controller.loadData();
        var speciesList = controller.getSpeciesList();

        assertEquals(1, speciesList.size());
        assertEquals("AAA.BBB",
                     speciesList
                             .get(0)
                             .speciesCode());
        assertEquals("Aaabin Bbbir",
                     speciesList
                             .get(0)
                             .speciesNameEng());
        assertEquals("Aaarus Bbbirix",
                     speciesList
                             .get(0)
                             .speciesNameLat());
    }

    @Test
    void getSpeciesNullData() throws Exception {
        var record1 = new NewDbBirdRecord();
        record1.SpeciesCode = "AAA.BBB";
        record1.EnglishName = "Aaabin Bbbir";
        record1.LatinName = "Aaarus Bbbirix";

        var record2 = new OldDbBirdRecord();
        record2.Spec = null;

        var record3 = new NewDbBirdRecord();
        record3.SpeciesCode = "-";

        var record4 = new NewDbBirdRecord();
        record4.SpeciesCode = "?";

        var record5 = new NewDbBirdRecord();
        record5.SpeciesCode = "";

        fileDataLoader.Data = List.of(DbBirdRecord.from(record1), DbBirdRecord.from(record2), DbBirdRecord.from(record3), DbBirdRecord.from(record4), DbBirdRecord.from(record5));

        controller.loadData();
        var speciesList = controller.getSpeciesList();

        assertEquals(1, speciesList.size());
    }

    @Test
    void getMultipleSpecies() throws Exception {
        var record1 = new NewDbBirdRecord();
        record1.SpeciesCode = "AAA.BBB";
        record1.EnglishName = "Aaabin Bbbir";
        record1.LatinName = "Aaarus Bbbirix";

        var record2 = new OldDbBirdRecord();
        record2.Spec = "BBB.CCC";

        var record3 = new NewDbBirdRecord();
        record3.SpeciesCode = "CCC.DDD";
        record3.EnglishName = "Cccin Dddir";
        record3.LatinName = "Cccus Dddirix";

        fileDataLoader.Data = List.of(DbBirdRecord.from(record1), DbBirdRecord.from(record2), DbBirdRecord.from(record3));

        controller.loadData();
        var speciesList = controller.getSpeciesList();

        assertEquals(3, speciesList.size());
        assertEquals("AAA.BBB",
                     speciesList
                             .get(0)
                             .speciesCode());
        assertEquals("Aaabin Bbbir",
                     speciesList
                             .get(0)
                             .speciesNameEng());
        assertEquals("Aaarus Bbbirix",
                     speciesList
                             .get(0)
                             .speciesNameLat());
        assertEquals("BBB.CCC",
                     speciesList
                             .get(1)
                             .speciesCode());
        assertEquals("CCC.DDD",
                     speciesList
                             .get(2)
                             .speciesCode());
        assertEquals("Cccin Dddir",
                     speciesList
                             .get(2)
                             .speciesNameEng());
        assertEquals("Cccus Dddirix",
                     speciesList
                             .get(2)
                             .speciesNameLat());
    }

    @Test
    void getRecordsCount() throws Exception {

        fileDataLoader.Data = List.of(new DbBirdRecord(), new DbBirdRecord(), new DbBirdRecord());

        controller.loadData();
        var recordsCount = controller.getRecordsCount();

        assertEquals(3, recordsCount);
    }

    @Test
    void getCalculatedData() throws Exception {
        var record1 = new NewDbBirdRecord();
        record1.SpeciesCode = "AAA.BBB";
        record1.EnglishName = "Aaabin Bbbir";
        record1.LatinName = "Aaarus Bbbirix";
        record1.Sex = "M";
        record1.Ring = "1234";

        var record2 = new OldDbBirdRecord();
        record2.Spec = "AAA.BBB";
        record2.Sex = "M";
        record2.Ring = "1235";

        var record3 = new OldDbBirdRecord();
        record3.Spec = "AAA.BBB";
        record3.Sex = "F";
        record3.Ring = "1236";

        var record4 = new NewDbBirdRecord();
        record4.SpeciesCode = "BBB.CCC";
        record4.Ring = "1237";

        fileDataLoader.Data = List.of(DbBirdRecord.from(record1), DbBirdRecord.from(record2), DbBirdRecord.from(record3), DbBirdRecord.from(record4));

        controller.loadData();
        controller.setSexSelected(BirdSex.Male);
        controller.setSpeciesSelected(new BirdSpecies("AAA.BBB", "Aaabin Bbbir", "Aaarus Bbbirix"));
        BirdSpeciesCalculatedData calculatedData = controller.getCalculatedData();

        assertEquals(2, calculatedData.recordsCount());
        assertEquals("AAA.BBB", calculatedData.speciesCode());
        assertEquals("Aaabin Bbbir", calculatedData.speciesNameEng());
        assertEquals("Aaarus Bbbirix", calculatedData.speciesNameLat());
        assertEquals("Samiec", calculatedData.selectedSexName());
        assertEquals(0, calculatedData.returnsCount());
    }

    @Test
    void getCalculatedDataAnySex() throws Exception {
        var record1 = new NewDbBirdRecord();
        record1.SpeciesCode = "AAA.BBB";
        record1.Sex = "M";
        record1.Ring = "1234";

        var record2 = new OldDbBirdRecord();
        record2.Spec = "AAA.BBB";
        record2.Sex = "M";
        record2.Ring = "1235";

        var record3 = new OldDbBirdRecord();
        record3.Spec = "AAA.BBB";
        record3.Sex = "F";
        record3.Ring = "1236";

        var record4 = new NewDbBirdRecord();
        record4.SpeciesCode = "BBB.CCC";
        record4.Ring = "1237";

        fileDataLoader.Data = List.of(DbBirdRecord.from(record1), DbBirdRecord.from(record2), DbBirdRecord.from(record3), DbBirdRecord.from(record4));

        controller.loadData();
        controller.setSexSelected(BirdSex.Any);
        controller.setSpeciesSelected(new BirdSpecies("AAA.BBB", "Aaabin Bbbir", "Aaarus Bbbirix"));
        BirdSpeciesCalculatedData calculatedData = controller.getCalculatedData();

        assertEquals(3, calculatedData.recordsCount());
        assertEquals("AAA.BBB", calculatedData.speciesCode());
        assertEquals("Dowolna", calculatedData.selectedSexName());
        assertEquals(0, calculatedData.returnsCount());
    }

    @Test
    public void getCalculatedDataReturningBirds() throws Exception {

        var record1 = new NewDbBirdRecord();
        record1.SpeciesCode = "XXX.YYY";
        record1.Ring = "1234";
        record1.Date2 = "2019-11-01";
        record1.Seas = "A";
        record1.Age = "I";

        var record2 = new OldDbBirdRecord();
        record2.Spec = "XXX.YYY";
        record2.Ring = "1235";
        record2.Date = LocalDateTime.of(2019, 11, 1, 0, 0);
        record2.Seas = "A";
        record2.Age = "I";

        var record3 = new OldDbBirdRecord();
        record3.Spec = "XXX.YYY";
        record3.Ring = "1234";
        record3.Date = LocalDateTime.of(2020, 3, 1, 0, 0);
        record3.Seas = "S";
        record3.Age = "I";

        var record4 = new NewDbBirdRecord();
        record4.SpeciesCode = "XXX.YYY";
        record4.Ring = "1237";
        record4.Date2 = "2020-03-02";
        record4.Seas = "S";
        record4.Age = "I";

        fileDataLoader.Data = List.of(DbBirdRecord.from(record1), DbBirdRecord.from(record2), DbBirdRecord.from(record3), DbBirdRecord.from(record4));

        controller.loadData();
        controller.setSexSelected(BirdSex.Any);
        controller.setSpeciesSelected(new BirdSpecies("XXX.YYY", "Aaabin Bbbir", "Aaarus Bbbirix"));
        BirdSpeciesCalculatedData calculatedData = controller.getCalculatedData();

        assertEquals(4, calculatedData.recordsCount());
        assertEquals("XXX.YYY", calculatedData.speciesCode());
        assertEquals("Dowolna", calculatedData.selectedSexName());
        assertEquals(1, calculatedData.returnsCount());
        assertEquals(4, calculatedData.recordsCount());
    }

    @Test
    public void returnsCountDifferentSeasonTest() throws Exception {

        var record1 = new NewDbBirdRecord();
        record1.SpeciesCode = "XXX.YYY";
        record1.Ring = "1234";
        record1.Date2 = "2019-11-01";
        record1.Seas = "A";
        record1.Age = "J";

        var r1 = DbBirdRecord.from(record1);

        var record2 = new OldDbBirdRecord();
        record2.Spec = "XXX.YYY";
        record2.Ring = "1234";
        record2.Date = LocalDateTime.of(2020, 3, 1, 0, 0);
        record2.Seas = "S";
        record2.Age = "J";
        var r2 = DbBirdRecord.from(record2);

        var record3 = new OldDbBirdRecord();
        record3.Spec = "XXX.YYY";
        var r3 = DbBirdRecord.from(record3);

        fileDataLoader.Data = List.of(r1, r2, r3);

        controller.loadData();
        controller.setSexSelected(BirdSex.Any);
        controller.setSpeciesSelected(new BirdSpecies("XXX.YYY", "Aaabin Bbbir", "Aaarus Bbbirix"));
        BirdSpeciesCalculatedData calculatedData = controller.getCalculatedData();

        assertEquals(3, calculatedData.recordsCount());
        assertEquals(1, calculatedData.returnsCount());
    }


    @Test
    public void writeResultsForSelectedSpeciesTest() throws Exception {
        var speciesFilter = new MockSpeciesFilter();
        speciesFilter.filteredRecords = List.of(DbBirdRecord.from(new NewDbBirdRecord()), DbBirdRecord.from(new NewDbBirdRecord()));

        controller.speciesFilter = speciesFilter;

        var returningBirdsSummarizer = new MockReturningBirdsSummarizer();
        returningBirdsSummarizer.summary = List.of(new ReturningBirdsData());
        controller.returningBirdsSummarizer = returningBirdsSummarizer;

        var mapper = new MockReturningBirdsDataCsvRecordMapper();
        mapper.outputData = List.of(new CsvReturningBirdsData(), new CsvReturningBirdsData());
        controller.mapper = mapper;

        var serializer = new MockCsvSerializer<CsvReturningBirdsData>();
        serializer.outputData = "abc";

        controller.serializer = serializer;

        var fileChooser = new MockFileChooser();
        fileChooser.FileName = "test.csv";
        controller.fileChooser = fileChooser;

        var csvFileWriter = new MockCsvFileSaver();

        controller.csvFileWriter = csvFileWriter;

        controller.setSpeciesSelected(new BirdSpecies("XXX.YYY", "Aaabin Bbbir", "Aaarus Bbbirix"));

        var result = controller.writeResultsForSelectedSpecies();

        // 1. Z danych jest wyodrębniany dany gatunek.
        assertTrue(speciesFilter.isFilterCalled);
        assertEquals("XXX.YYY", speciesFilter.speciesToFilter.speciesCode());
        assertEquals("Aaabin Bbbir", speciesFilter.speciesToFilter.speciesNameEng());
        assertEquals("Aaarus Bbbirix", speciesFilter.speciesToFilter.speciesNameLat());

        // 2. Tworzone jest podsumowanie przez serwis.
        assertTrue(returningBirdsSummarizer.isCreateSummaryCalled);
        assertEquals(2, returningBirdsSummarizer.inputData.size());

        // 3. Dane są mapowane do struktury CSV.
        assertTrue(mapper.isMapperCalled);
        assertEquals(1, mapper.inputData.size());

        // 4. Struktura CSV jest przetwarzane do ciągu znaków przez serializer.
        assertTrue(serializer.isSerializeCalled);
        assertEquals(2, serializer.inputData.size());

        // 5. Od użytkownika jest pobierana nazwa pliku.
        assertTrue(fileChooser.isSelectFileToSaveCalled);

        // 6. Podsumowanie jest zapisywane do pliku.
        assertTrue(csvFileWriter.isSaveToFileCalled);
        assertEquals("test.csv", csvFileWriter.FileName);
        assertEquals("abc", csvFileWriter.fileData);

        assertEquals("test.csv", result.OutputFileName);
        assertEquals(2, result.RecordsCount);
    }

    @Test
    public void writeResultsForAllSpeciesTest() throws Exception {
        controller.data = List.of(DbBirdRecord.from(new NewDbBirdRecord()), DbBirdRecord.from(new NewDbBirdRecord()), DbBirdRecord.from(new NewDbBirdRecord()));

        var speciesFilter = new MockSpeciesFilter();
        controller.speciesFilter = speciesFilter;

        var returningBirdsSummarizer = new MockReturningBirdsSummarizer();
        returningBirdsSummarizer.summary = List.of(new ReturningBirdsData());
        controller.returningBirdsSummarizer = returningBirdsSummarizer;

        var mapper = new MockReturningBirdsDataCsvRecordMapper();
        mapper.outputData = List.of(new CsvReturningBirdsData(), new CsvReturningBirdsData(), new CsvReturningBirdsData(), new CsvReturningBirdsData());
        controller.mapper = mapper;

        var serializer = new MockCsvSerializer<CsvReturningBirdsData>();
        serializer.outputData = "abc";
        controller.serializer = serializer;

        var fileChooser = new MockFileChooser();
        fileChooser.FileName = "test.csv";
        controller.fileChooser = fileChooser;

        var csvFileWriter = new MockCsvFileSaver();
        controller.csvFileWriter = csvFileWriter;

        controller.setSpeciesSelected(new BirdSpecies("XXX.YYY", "Aaabin Bbbir", "Aaarus Bbbirix"));

        var result = controller.writeResultsForAllSpecies();

        // 1. Z danych nie jest wyodrębniany gatunek.
        assertFalse(speciesFilter.isFilterCalled);

        // 2. Tworzone jest podsumowanie przez serwis.
        assertTrue(returningBirdsSummarizer.isCreateSummaryCalled);
        assertEquals(3, returningBirdsSummarizer.inputData.size());

        // 3. Dane są mapowane do struktury CSV.
        assertTrue(mapper.isMapperCalled);
        assertEquals(1, mapper.inputData.size());

        // 4. Struktura CSV jest przetwarzane do ciągu znaków przez serializer.
        assertTrue(serializer.isSerializeCalled);
        assertEquals(4, serializer.inputData.size());

        // 5. Od użytkownika jest pobierana nazwa pliku.
        assertTrue(fileChooser.isSelectFileToSaveCalled);

        // 6. Podsumowanie jest zapisywane do pliku.
        assertTrue(csvFileWriter.isSaveToFileCalled);
        assertEquals("test.csv", csvFileWriter.FileName);
        assertEquals("abc", csvFileWriter.fileData);

        assertEquals("test.csv", result.OutputFileName);
        assertEquals(4, result.RecordsCount);
    }
}

