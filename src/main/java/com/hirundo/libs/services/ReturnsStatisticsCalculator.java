package com.hirundo.libs.services;

import com.hirundo.libs.data_structures.BirdSex;
import com.hirundo.libs.data_structures.BirdSpecies;
import com.hirundo.libs.data_structures.BirdSpeciesCalculatedData;
import com.hirundo.libs.data_structures.DbBirdRecord;

import java.util.List;

public class ReturnsStatisticsCalculator {
    ReturningBirdsSummarizer returningBirdsSummarizer = new ReturningBirdsSummarizer();

    public BirdSpeciesCalculatedData getCalculatedData(List<DbBirdRecord> data, BirdSpecies selectedSpecies, BirdSex selectedSex) throws Exception {

        if (null == selectedSpecies) {
            throw new Exception("Species not selected");
        }
        if (BirdSex.Undefined == selectedSex) {
            throw new Exception("Sex not selected");
        }

        var speciesCode = selectedSpecies.speciesCode();

        if (null == speciesCode || speciesCode.isBlank()) {
            throw new Exception("Species code not selected");
        }


        var speciesNameEng = selectedSpecies.speciesNameEng();
        var speciesNameLat = selectedSpecies.speciesNameLat();
        var sexName = getSexName(selectedSex);

        var filteredData = data
                .stream()
                .filter(b -> speciesCode.equals(b.getSpeciesCode()));

        if (BirdSex.Any != selectedSex) {
            filteredData = filteredData.filter(b -> selectedSex == b.getSex());
        }

        var list = filteredData.toList();

        var recordsCount = list.size();

        var returningBirds = returningBirdsSummarizer.getSummary(list);

        var returnsCount = returningBirds.size();

        return new BirdSpeciesCalculatedData(speciesCode,
                                             speciesNameEng,
                                             speciesNameLat,
                                             sexName,
                                             recordsCount,
                                             returnsCount);
    }

    private String getSexName(BirdSex selectedSex) {
        return switch (selectedSex) {
            case Male -> "Samiec";
            case Female -> "Samica";
            case Any -> "Dowolna";
            default -> "Nieznana";
        };
    }
}
