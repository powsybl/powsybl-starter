# PowSyBl Starter



PowSyBl repository versions for the 2025.3.1 powsybl-starter release correspond to the ones of
[powsybl-dependencies 2025.3.1](https://github.com/powsybl/powsybl-dependencies/releases/tag/v2025.3.1).



## Getting started

To start using PowSyBl components in a Maven project, you just have to include one dependency:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-starter</artifactId>
    <version>2025.3.1</version>
</dependency>
```



## Examples



Create an IEEE 14 buses, run an AC load flow with default parameters, print calculation status and buses voltage magnitude:

```java
Network network = IeeeCdfNetworkFactory.create14();
LoadFlowResult result = LoadFlow.run(network);
System.out.println(result.getComponentResults().getFirst().getStatus());
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

Import a CGMES model in a single step. 
The model is defined by the files `20251211T0000Z_1D_TSO_EQ_000`  and `20251211T0000Z_1D_TSO_SSH_000`:

```java
Path pathFile = Paths.get("/path/to/cgmes_model");
Network network = Network.read(new DirectoryDataSource(pathFile, "20251211T0000Z_1D_TSO"));
```

Import a CGMES model in two steps: first, import only the EQ file, and then update the model by reading the SSH file. 
The model is defined by the files `20251211T0000Z_1D_TSO_EQ_000`  and `20251211T0000Z_1D_TSO_SSH_000`:

```java
Path pathFile = Paths.get("/path/to/cgmes_model");
Network network = Network.read(new DirectoryDataSource(pathFile, "20251211T0000Z_1D_TSO_EQ"));
network.update(new DirectoryDataSource(pathFile, "20251211T0000Z_1D_TSO_SSH"));
```

Import a CGMES model consisting of one EQ file (`20251211T0000Z_1D_TSO_EQ_000`) and four SSH files: 
midnight (`20251211T0000Z_1D_TSO_SSH_000`), morning (`20251211T0800Z_1D_TSO_SSH_000`), afternoon (`20251211T1600Z_1D_TSO_SSH_000`), and end of day (`20251211T2400Z_1D_TSO_SSH_000`). 
The midnight SSH file is complete and contains data for all equipment, while the remaining SSH files are partial, including only changes relative to the previous SSH file. 
The entire process is carried out in four steps, using a single variant:

```java
Path pathFile = Paths.get("/path/to/cgmes_model");

// Import the midnight EQ and SSH files
Path pathFile = Paths.get("/work/tmp/cgmes_update/partial");
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
```

Import a CGMES model consisting of one EQ file (`20251210T0000Z_1D_TSO_EQ_000`) and four SSH files: midnight (`20251210T0000Z_1D_TSO_SSH_000`), morning (`20251210T0800Z_1D_TSO_SSH_000`), afternoon (`20251210T1600Z_1D_TSO_SSH_000`), and end of day (`20251210T2400Z_1D_TSO_SSH_000`). 
All SSH files are complete and contain data for all equipment. 
The entire process is carried out in four steps, using a separate variant for each SSH file:

```java
Path pathFile = Paths.get("/path/to/cgmes_model");

// Import the midnight EQ and SSH files
Path pathFile = Paths.get("/work/tmp/cgmes_update/variant");
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
SensitivityAnalysisRunParameters runParameters = new SensitivityAnalysisRunParameters()
        .setContingencies(contingencies)
        .setParameters(parameters);
SensitivityAnalysisResult result = SensitivityAnalysis.run(network, factors, runParameters);
```

