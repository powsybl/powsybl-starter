package com.powsybl.starter;

import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.sensitivity.*;
import com.powsybl.sld.SingleLineDiagram;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class ReadmeTest {

    private @NonNull Path getPathFile(String path) throws URISyntaxException {
        return Paths.get(getResourceURI(path));
    }

    private @NonNull String getFullPath(String path) throws URISyntaxException {
        return Path.of(getResourceURI(path)).toString();
    }

    private @NonNull URI getResourceURI(String path) throws URISyntaxException {
        var tmp = path;
        if (!path.startsWith("/")) {
            tmp = "/" + tmp;
        }
        String osIndependentPath = tmp.replace("/", File.separator);
        URL resource = getClass().getResource(osIndependentPath);
        if (resource == null) {
            throw new IllegalStateException("Test resource directory not found: " + osIndependentPath);
        }
        return resource.toURI();
    }

    @Test
    void testExample1() {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Network network = IeeeCdfNetworkFactory.create14();
        LoadFlowResult result = LoadFlow.run(network);
        System.out.println(result.getComponentResults().getFirst().getStatus());
        network.getBusView().getBusStream().forEach(bus -> System.out.println(bus.getId() + " " + bus.getV()));
    }

    @Test
    void testExample2() {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Network network = IeeeCdfNetworkFactory.create14();
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setDc(true);
        LoadFlow.run(network, parameters);
    }

    @Test
    void testExample3() throws URISyntaxException {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Network network = Network.read(getFullPath("IEEE_118_bus.raw"));
        LoadFlow.run(network);
        SingleLineDiagram.draw(network, "VL7", "vl7.svg");
    }

    @Test
    void testExample4() throws URISyntaxException {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Network network = Network.read(getFullPath("simple-eu.uct"));
        LoadFlow.run(network);
        NetworkAreaDiagram.draw(network, Path.of("simple-eu.svg"));
    }

    @Test
    void testExample5() throws URISyntaxException {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Network network = Network.read(getFullPath("simple-eu.uct"));
        List<Contingency> contingencies = network.getLineStream().map(l -> Contingency.line(l.getId())).toList();
        SecurityAnalysisResult result = SecurityAnalysis.run(network, contingencies).getResult();
    }

    @Test
    void testCgmesImportAll() throws URISyntaxException {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Path pathFile = getPathFile("/cgmes_model/simple");
        Network network = Network.read(new DirectoryDataSource(pathFile, "20251211T0000Z_1D_TSO"));
    }

    @Test
    void testCgmesImportEqThenSsh() throws URISyntaxException {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Path pathFile = getPathFile("/cgmes_model/simple");
        Network network = Network.read(new DirectoryDataSource(pathFile, "20251211T0000Z_1D_TSO_EQ"));
        network.update(new DirectoryDataSource(pathFile, "20251211T0000Z_1D_TSO_SSH"));
    }

    @Test
    void testCgmesPartialSshUpdate() throws URISyntaxException {
        // Import the midnight EQ and SSH files
        Path pathFile = getPathFile("/cgmes_model/partial");
        Network network = Network.read(new DirectoryDataSource(pathFile, "20251211T0000Z_1D_TSO"));

        // Use previous values to fill in missing data in the partial SSH files using previous values
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");

        // Update the model by importing the morning SSH file
        network.update(new DirectoryDataSource(pathFile, "20251211T0800Z_1D_TSO_SSH"), properties);

        // Update the model by importing the afternoon SSH file
        network.update(new DirectoryDataSource(pathFile, "20251211T1600Z_1D_TSO_SSH"), properties);

        // Update the model by importing the end of the day SSH file
        network.update(new DirectoryDataSource(pathFile, "20251211T2400Z_1D_TSO_SSH"), properties);
    }

    @Test
    void testCgmesSshUpdateWithVariants() throws URISyntaxException {
        // Import the midnight EQ and SSH files
        Path pathFile = getPathFile("/cgmes_model/variant");
        Network network = Network.read(new DirectoryDataSource(pathFile, "20251210T0000Z_1D_TSO"));

        // Update the model by importing the morning SSH file into a new variant
        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "morning");
        network.getVariantManager().setWorkingVariant("morning");
        network.update(new DirectoryDataSource(pathFile, "20251210T0800Z_1D_TSO_SSH"));

        // Update the model by importing the afternoon SSH file into a new variant
        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "afternoon");
        network.getVariantManager().setWorkingVariant("afternoon");
        network.update(new DirectoryDataSource(pathFile, "20251210T1600Z_1D_TSO_SSH"));

        // Update the model by importing the end of the day SSH file into a new variant
        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "end-of-the-day");
        network.getVariantManager().setWorkingVariant("end-of-the-day");
        network.update(new DirectoryDataSource(pathFile, "20251210T2400Z_1D_TSO_SSH"));
    }

    @Test
    void testExample6() throws URISyntaxException {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Network networkBe = Network.read(getFullPath("BE_with_EQ_BD.zip"));
        Network networkNl = Network.read(getFullPath("NL_with_EQ_BD.zip"));
        Network merged = Network.merge("mergedBeNl", networkNl, networkBe);
        LoadFlow.run(merged);
    }

    @Test
    void testExample7() throws URISyntaxException {
        // The README.md file must be updated when this method is modified
        // ===============================================================
        Network network = Network.read(getFullPath("simple-eu.uct"));
        List<SensitivityFactor> factors = new ArrayList<>();
        for (Generator g : network.getGenerators()) {
            for (Line l : network.getLines()) {
                factors.add(new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, l.getId(),
                        SensitivityVariableType.INJECTION_ACTIVE_POWER, g.getId(),
                        false, ContingencyContext.all()));
            }
        }
        List<Contingency> contingencies = network.getLineStream().map(l -> Contingency.line(l.getId())).toList();
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.getLoadFlowParameters().setDc(true);
        SensitivityAnalysisRunParameters runParameters = new SensitivityAnalysisRunParameters()
                .setContingencies(contingencies)
                .setParameters(parameters);
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, factors, runParameters);
    }

}
