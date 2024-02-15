# PowSyBl Starter



PowSyBl repository versions for the 2024.0.1 powsybl-starter release correspond to the ones of
[powsybl-dependencies 2024.0.1](https://github.com/powsybl/powsybl-dependencies/releases/tag/v2024.0.1).



## Getting started

To start using PowSyBl components in a Maven project, you just have to include one dependency:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-starter</artifactId>
    <version>2024.0.1</version>
</dependency>
```



## Examples



Create an IEEE 14 buses, run an AC load flow with default parameters, print calculation status and buses voltage magnitude:

```java
Network network = IeeeCdfNetworkFactory.create14();
LoadFlowResult result = LoadFlow.run(network);
System.out.println(result.getComponentResults().get(0).getStatus());
network.getBusView().getBusStream().forEach(bus -> System.out.println(bus.getId() + " " + bus.getV()));
```



Run a DC load flow instead of an AC one:

```java
Network network = IeeeCdfNetworkFactory.create14();
LoadFlowParameters parameters = new LoadFlowParameters()
    .setDc(true);
LoadFlow.run(network, parameters);
```



Load a PSS/E raw file, run an AC load flow and generate a single line diagram for voltage level 'VL8':

```java
Network network = Network.read("IEEE_118_bus.raw");
LoadFlow.run(network);
SingleLineDiagram.draw(network, "VL7", "vl7.svg");
```

 

Load a UCTE file, run an AC load flow and generate a full network diagram:

```java
Network network = Network.read("simple-eu.uct");
LoadFlow.run(network);
NetworkAreaDiagram.draw(network, Path.of("simple-eu.svg"));
```



Load a UCTE file, run an AC security analysis with all N-1 line contingencies:

```java
Network network = Network.read("simple-eu.uct");
List<Contingency> contingencies = network.getLineStream().map(l -> Contingency.line(l.getId())).collect(Collectors.toList());
SecurityAnalysisResult result = SecurityAnalysis.run(network, contingencies).getResult();
```



Load 2 CGMES files, merge both networks and run a load flow on merged network:

```java
Network networkBe = Network.read("CGMES_v2_4_15_MicroGridTestConfiguration_BC_BE_v2.zip");
Network networkNl = Network.read("CGMES_v2_4_15_MicroGridTestConfiguration_BC_NL_v2.zip");
Network merged = Network.merge("mergedBeNl", networkNl, networkBe);
LoadFlow.run(merged);
```



Load a UCTE file and run a DC sensivity analysis of all generators active power injection on all branches active power flow for the pre-contingency state and for all N-1 line post-contingency states.

```java
Network network = Network.read("simple-eu.uct");
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
```

