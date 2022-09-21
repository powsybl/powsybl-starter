package com.powsybl.starter;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.ieeecdf.converter.IeeeCdfNetworkFactory;
import com.powsybl.iidm.import_.Importers;
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
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ReadmeTest {

    @Test
    void testExample1() {
        Network network = IeeeCdfNetworkFactory.create14();
        LoadFlowResult result = LoadFlow.run(network);
        System.out.println(result.getComponentResults().get(0).getStatus());
        network.getBusView().getBusStream().forEach(bus -> System.out.println(bus.getId() + " " + bus.getV()));
    }

    @Test
    void testExample2() {
        Network network = IeeeCdfNetworkFactory.create14();
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setDc(true);
        LoadFlow.run(network, parameters);
    }

    @Test
    void testExample3() {
        Network network = Importers.loadNetwork("IEEE_118_bus.raw");
        LoadFlow.run(network);
        SingleLineDiagram.draw(network, "VL8", "vl8.svg");
    }

    @Test
    void testExample4() {
        Network network = Importers.loadNetwork("simple-eu.uct");
        LoadFlow.run(network);
        new NetworkAreaDiagram(network).draw(Paths.get("simple-eu.svg"));
    }

    @Test
    void testExample5() {
        Network network = Importers.loadNetwork("simple-eu.uct");
        List<Contingency> contingencies = network.getLineStream().map(l -> Contingency.line(l.getId())).collect(Collectors.toList());
        SecurityAnalysisResult result = SecurityAnalysis.run(network, contingencies).getResult();
    }

    @Test
    void testExample6() {
        Network networkBe = Importers.loadNetwork("CGMES_v2_4_15_MicroGridTestConfiguration_BC_BE_v2.zip");
        Network networkNl = Importers.loadNetwork("CGMES_v2_4_15_MicroGridTestConfiguration_BC_NL_v2.zip");
        networkBe.merge(networkNl);
        LoadFlow.run(networkBe);
    }

    @Test
    void testExample7() {
        Network network = Importers.loadNetwork("simple-eu.uct");
        List<SensitivityFactor> factors = new ArrayList<>();
        for (Generator g : network.getGenerators()) {
            for (Line l : network.getLines()) {
                factors.add(new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1, l.getId(),
                        SensitivityVariableType.INJECTION_ACTIVE_POWER, g.getId(),
                        false, ContingencyContext.all()));
            }
        }
        List<Contingency> contingencies = network.getLineStream().map(l -> Contingency.line(l.getId())).collect(Collectors.toList());
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.getLoadFlowParameters().setDc(true);
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, factors, contingencies, parameters);
    }

}
