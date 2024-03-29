package com.hirundo.app.controllers;

import com.hirundo.app.models.FileSaveResult;
import com.hirundo.app.models.services.CsvFileWriter;
import com.hirundo.app.models.services.ICsvFileWriter;
import com.hirundo.app.models.services.IFileChooser;
import com.hirundo.libs.data_structures.*;
import com.hirundo.libs.filters.ISpeciesFilter;
import com.hirundo.libs.filters.SpeciesFilter;
import com.hirundo.libs.loaders.IBirdRecordDataLoaderBuilder;
import com.hirundo.libs.mappers.IReturningBirdsDataCsvRecordMapper;
import com.hirundo.libs.mappers.ReturningBirdsDataCsvRecordMapper;
import com.hirundo.libs.serializers.CsvSerializer;
import com.hirundo.libs.serializers.ICsvSerializer;
import com.hirundo.libs.services.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainController {
    private final IBirdRecordDataLoaderBuilder builder;
    public List<DbBirdRecord> data = new ArrayList<>();
    public IFileChooser fileChooser;
    public ISpeciesFilter speciesFilter = new SpeciesFilter();
    public IReturningBirdsSummarizer returningBirdsSummarizer = new ReturningBirdsSummarizer();
    public IReturningBirdsDataCsvRecordMapper mapper = new ReturningBirdsDataCsvRecordMapper();
    public ICsvSerializer<CsvReturningBirdsData> serializer = new CsvSerializer<>(CsvReturningBirdsData.class);
    public ICsvFileWriter csvFileWriter = new CsvFileWriter();
    BirdSpecies selectedSpecies;
    BirdSex selectedSex = BirdSex.Any;
    ReturnsStatisticsCalculator calculator = new ReturnsStatisticsCalculator();
    private String selectedFileName;
    private String oldTableName;
    private String newTableName;
    private LocalDate dateRangeStart;
    private LocalDate dateRangeEnd;
    private boolean isDateRangeSelected;

    public MainController(IBirdRecordDataLoaderBuilder builder, IFileChooser fileChooser) {
        this.builder = builder;
        this.fileChooser = fileChooser;
    }

    public void loadData() throws Exception {
        var dataLoader = builder
                .withFilename(selectedFileName)
                .withOldTableName(oldTableName)
                .withNewTableName(newTableName)
                .build();
        data = dataLoader.loadData();
    }

    public BirdSpeciesCalculatedData getCalculatedData() throws Exception {
        var parameters = getReturnsStatisticsCalculatorParameters();
        return calculator.getCalculatedData(data, parameters);
    }

    public String selectFileName() {
        var file = fileChooser.selectFileToOpen();
        if (null != file) {
            selectedFileName = file;
        }
        return selectedFileName;
    }

    public String getSelectedFileName() {
        return selectedFileName;
    }

    public void setNewTableName(String value) {
        this.newTableName = value;
    }

    public void setOldTableName(String value) {
        this.oldTableName = value;
    }

    public List<BirdSpecies> getSpeciesList() {
        return speciesFilter.getSpeciesList(data);
    }

    public Integer getRecordsCount() {
        return data.size();
    }

    public void setSpeciesSelected(BirdSpecies species) {
        this.selectedSpecies = species;
    }

    public void setSexSelected(BirdSex sex) {
        selectedSex = sex;
    }

    public FileSaveResult writeResultsForSelectedSpecies() throws Exception {
        String filename;

        try {
            var templateFilename = getTemplateFilename(true);
            filename = fileChooser.selectFileToSave(templateFilename);
        } catch (Exception e) {
            throw new Exception("Error while selecting file. " + e.getMessage());
        }

        if (null == filename) {
            return null;
        }

        List<DbBirdRecord> filteredResults;

        try {
            filteredResults = speciesFilter.filterBySpecies(data, selectedSpecies);
        } catch (Exception e) {
            throw new Exception("Error while filtering data. " + e.getMessage());
        }

        return getFileSaveResult(filename, filteredResults);
    }


    public FileSaveResult writeResultsForAllSpecies() throws Exception {
        var templateFilename = getTemplateFilename(false);
        var filename = fileChooser.selectFileToSave(templateFilename);
        if (null == filename) {
            return null;
        }

        return getFileSaveResult(filename, data);
    }

    String getTemplateFilename(boolean isSpeciesSelected) {
        var speciesName = "";

        if (!isSpeciesSelected) {
            speciesName = "all-species";
        } else {
            speciesName = selectedSpecies
                    .speciesCode()
                    .replace(".", "-");

        }

        String sexName = getSexName();

        String dateRangeName = getDateRangeName();

        return String.format("%s-%s-%s.csv", speciesName, sexName, dateRangeName);
    }

    private String getSexName() {
        var sexName = "any-sex";

        if (BirdSex.Any != selectedSex) {
            sexName = selectedSex
                    .toString()
                    .toLowerCase();
        }
        return sexName;
    }

    private String getDateRangeName() {
        var dateRangeName = "all-dates";

        if (isDateRangeSelected) {
            try {
                var dateStart = DateTimeFormatter
                        .ofPattern("MM-dd", Locale.ENGLISH)
                        .format(dateRangeStart);

                var dateEnd = DateTimeFormatter
                        .ofPattern("MM-dd", Locale.ENGLISH)
                        .format(dateRangeEnd);

                dateRangeName = String.format("from-%s-to-%s", dateStart, dateEnd);
            }
            catch (Exception e){
                throw new IllegalArgumentException("Error while formatting date range. " + e.getMessage());
            }
        }
        return dateRangeName;
    }

    private FileSaveResult getFileSaveResult(String filename, List<DbBirdRecord> filteredResults) throws Exception {
        String result;
        List<CsvReturningBirdsData> mappedData;

        try {
            var parameters = getReturningBirdsSummarizerParameters();
            var returningData = returningBirdsSummarizer.getSummary(filteredResults, parameters);
            mappedData = mapper.getCsvReturningBirdsData(returningData);
            result = serializer.serializeToCsv(mappedData);
        } catch (Exception e) {
            throw new Exception("Error while serializing data", e);
        }

        try {
            csvFileWriter.writeToFile(filename, result);
            var fileSaveResult = new FileSaveResult();
            fileSaveResult.OutputFileName = filename;
            fileSaveResult.RecordsCount = mappedData.size();
            return fileSaveResult;
        } catch (Exception e) {
            throw new Exception("Error while saving file", e);
        }
    }


    private ReturningBirdsSummarizerParameters getReturningBirdsSummarizerParameters() {
        var parameters = new ReturningBirdsSummarizerParameters();
        parameters.useDateRange = isDateRangeSelected;
        parameters.dateRangeStart = dateRangeStart;
        parameters.dateRangeEnd = dateRangeEnd;
        parameters.sex = selectedSex;
        return parameters;
    }

    private ReturnsStatisticsCalculatorParameters getReturnsStatisticsCalculatorParameters() {
        var parameters = new ReturnsStatisticsCalculatorParameters();
        parameters.species = selectedSpecies;
        parameters.selectedSex = selectedSex;
        parameters.dateRangeStart = dateRangeStart;
        parameters.dateRangeEnd = dateRangeEnd;
        parameters.isDateRangeSelected = isDateRangeSelected;
        return parameters;
    }

    public void setDateRangeStart(LocalDate dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    public void setDateRangeEnd(LocalDate dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }

    public void setIsDateRangeSelected(boolean selected) {
        this.isDateRangeSelected = selected;
    }
}

