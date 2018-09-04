import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.MoreObjects.firstNonNull;

public class RunCommandBuilder {

    private final Properties properties;
    private final Collection<Map.Entry<String,String>> environmentVariables = new HashSet<>();
    private final Collection<Map.Entry<String,String>> volumes = new HashSet<>();
    private final Collection<Map.Entry<String,String>> ports = new HashSet<>();
    private final Collection<Map.Entry<String,String>> links = new HashSet<>();

    public RunCommandBuilder(Properties properties) {
        this.properties = properties;
        grabPropertiesWithPrefixAndAddToCollection("env", environmentVariables);
        grabPropertiesWithPrefixAndAddToCollection("vol", volumes);
        grabPropertiesWithPrefixAndAddToCollection("port", ports);
        grabPropertiesWithPrefixAndAddToCollection("link", links);
    }

    public RunCommandBuilder(String propertiesString) throws IOException {
        this(convertStringToProperties(propertiesString));
    }

    private static Properties convertStringToProperties(String propertiesString)
        throws IOException {
        Properties myProperties = new Properties();
        try(StringReader stringReader = new StringReader(propertiesString)) {
            myProperties.load(stringReader);
        }
        return myProperties;
    }

    private void grabPropertyAsCommaSeperatedList(final String key, final Collection<String> collection) {
        String value = properties.getProperty(key);

        if (value != null) {
            collection.addAll(Splitter.on(",").splitToList(value));
        }
    }

    private void grabPropertiesWithPrefixAndAddToCollection(final String prefix,
        final Collection<Map.Entry<String, String>> collection) {

        final String prefixPlusDot = prefix + ".";
        collection.addAll(Sets.newHashSet(Iterables.transform(
            Iterables.filter(properties.entrySet(), new Predicate<Map.Entry<Object, Object>>() {
                @Override public boolean apply(Map.Entry<Object, Object> entry) {
                    return entry.getKey().toString().startsWith(prefixPlusDot);
                }
            }), new Function<Map.Entry<Object, Object>, Map.Entry<String, String>>() {
                @Override public Map.Entry<String, String> apply(Map.Entry<Object, Object> entry) {
                    return new AbstractMap.SimpleEntry<>(entry.getKey()
                        .toString().substring(prefixPlusDot.length()),
                        entry.getValue().toString());
                }
            })));

    }

    public Collection<Map.Entry<String, String>> getEnvironmentVariables() {
        return environmentVariables;
    }

    public Collection<Map.Entry<String, String>> getVolumes() {
        return volumes;
    }

    public Collection<Map.Entry<String, String>> getPorts() {
        return ports;
    }

    public String getName() {
        return properties.getProperty("name");
    }

    public String getUser() {
        return properties.getProperty("user");
    }

    public String getWorkDir() {
        return properties.getProperty("workDir");
    }

    public String getEntryPoint() {
        return properties.getProperty("entryPoint");
    }

    public String getRestart() {
        return firstNonNull(properties.getProperty("restart"), "always");
    }

    public Collection<Map.Entry<String, String>> getLinks() {
        return links;
    }

    public String toDockerRunCommand(final String image) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("docker run \\\n");


        if (getName() != null) {
            stringBuilder.append("--name '" + getName() + "' \\\n");
        }

        if (getUser() != null) {
            stringBuilder.append("--user " + getUser() + " \\\n");
        }

        if (getEntryPoint() != null) {
            stringBuilder.append("--entrypoint '" + getEntryPoint() + "' \\\n");
        }

        if (getRestart() != null) {
            stringBuilder.append("--restart " + getRestart() + " \\\n");
        }

        if (getWorkDir() != null) {
            stringBuilder.append("-w '" + getWorkDir() + "' \\\n");
        }

        for (Map.Entry<String,String> port : ports) {
            stringBuilder.append("-p " + port.getKey() +":" + port.getValue() + " \\\n");
        }

        for (Map.Entry<String,String> environmentVariable : environmentVariables) {
            stringBuilder.append("-e " + environmentVariable.getKey() + "='" + environmentVariable.getValue() + "' \\\n");
        }

        for (Map.Entry<String,String> volume : volumes) {
            stringBuilder.append("-v " + volume.getKey() + ":" + volume.getValue() + " \\\n");
        }

        for (Map.Entry<String,String> link : links) {
            stringBuilder.append("--link " + link.getKey() + ":" + link.getValue() + " \\\n");
        }

        stringBuilder.append(image);

        return stringBuilder.toString();
    }
}
