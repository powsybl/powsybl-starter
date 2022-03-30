# PowSyBl Starter



## Getting started

To start using PowSyBl components in a Maven project, you just have to include one dependency:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```



## Examples



Create a IEEE 14 buses, run an AC load flow with default parameters, print calculation status and buses voltage magnitude:

```java
Network network = IeeeCdfNetworkFactory.create14();
LoadFlowResult result = LoadFlow.run(network);
System.out.println(result.getComponentResults().get(0).getStatus());
network.getBusView().getBusStream().forEach(bus -> System.out.println(bus.getId() + " " + bus.getV()));
```



Run a DC load flow instead of an AC:

```java
Network network = IeeeCdfNetworkFactory.create14();
LoadFlowParameters parameters = new LoadFlowParameters()
    .setDc(true);
LoadFlow.run(network, parameters);
```



Load a PSS/E raw file, run an AC load flow and generate a single line diagram for voltage level 'VL8':

```java
Network network = Importers.loadNetwork("IEEE_118_bus.raw");
LoadFlow.run(network);
SingleLineDiagram.draw(network, "VL8", "vl8.svg");
```

 

Load a UCTE file, run an AC load flow and generate an full network diagram:

```java
Network network = Importers.loadNetwork("simple-eu.uct");
LoadFlow.run(network);
new NetworkAreaDiagram(network).draw(Paths.get("simple-eu.svg"));
```



Load a UCTE file, run an AC security analysis with all N-1 line contingencies:

```java
Network network = Importers.loadNetwork("simple-eu.uct");
List<Contingency> contingencies = network.getLineStream().map(l -> Contingency.line(l.getId())).collect(Collectors.toList());
SecurityAnalysisResult result = SecurityAnalysis.run(network, contingencies).getResult();
```



Load 2 CGMES files, merge both networks and run a load flow on merged network:

```java
Network networkBe = Importers.loadNetwork("CGMES_v2_4_15_MicroGridTestConfiguration_BC_BE_v2.zip");
Network networkNl = Importers.loadNetwork("CGMES_v2_4_15_MicroGridTestConfiguration_BC_NL_v2.zip");
networkBe.merge(networkNl);
LoadFlow.run(networkBe);
```



Load a UCTE file and run a DC sensivity analysis of all generator active power injection on all branches active power flow for the pre-contingency state and for all N-1 line post-contingency states.

```java
Network network = Importers.loadNetwork("simple-eu.uct");
List<SensitivityFactor> factors = new ArrayList<>();
for (Generator g : network.getGenerators()) {
    for (Line l : network.getLines()) {
        factors.add(new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER, l.getId(),
                                          SensitivityVariableType.INJECTION_ACTIVE_POWER, g.getId(),
                                          false, ContingencyContext.all()));
    }
}
List<Contingency> contingencies = network.getLineStream().map(l -> Contingency.line(l.getId())).collect(Collectors.toList());
SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
parameters.getLoadFlowParameters().setDc(true);
SensitivityAnalysisResult result = SensitivityAnalysis.run(network, factors, contingencies, parameters);
```

