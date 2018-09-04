import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unchecked")
public class RunCommandBuilderTest {


    @Test
    public void environmentVariables() throws IOException {
        String propertiesString = ""
            + "env.rg.config.test=var\n"
            + "env.rg.config.test2=var2";

        RunCommandBuilder runCommandBuilder = new RunCommandBuilder(propertiesString);
        assertThat(runCommandBuilder.getEnvironmentVariables(), Matchers.<Map.Entry<String,String>>hasSize(2));
        assertThat(runCommandBuilder.getEnvironmentVariables(), Matchers.<Map.Entry<String,String>>containsInAnyOrder(new AbstractMap.SimpleEntry<>("rg.config.test", "var"),
                 new AbstractMap.SimpleEntry<>("rg.config.test2", "var2")));
    }


    @Test
    public void volumes() throws IOException {
        String propertiesString = ""
            + "vol./export/data=/vol1\n"
            + "vol./export/data2=/vol2";

        RunCommandBuilder runCommandBuilder = new RunCommandBuilder(propertiesString);
        assertThat(runCommandBuilder.getVolumes(), Matchers.<Map.Entry<String,String>>hasSize(2));
        assertThat(runCommandBuilder.getVolumes(), Matchers.<Map.Entry<String,String>>containsInAnyOrder(new AbstractMap.SimpleEntry<>("/export/data", "/vol1"),
            new AbstractMap.SimpleEntry<>("/export/data2", "/vol2")));
    }

    @Test
    public void ports() throws IOException {
        String propertiesString = ""
            + "port.8080=8080\n"
            + "port.5005=5004";

        RunCommandBuilder runCommandBuilder = new RunCommandBuilder(propertiesString);
        assertThat(runCommandBuilder.getPorts(), Matchers.<Map.Entry<String,String>>hasSize(2));
        assertThat(runCommandBuilder.getPorts(), Matchers.<Map.Entry<String,String>>containsInAnyOrder(new AbstractMap.SimpleEntry<>("8080", "8080"),
            new AbstractMap.SimpleEntry<>("5005", "5004")));
    }


    @Test
    public void links() throws IOException {
        String propertiesString = ""
            + "link.link1=link1\n"
            + "link.link2=link2";

        RunCommandBuilder runCommandBuilder = new RunCommandBuilder(propertiesString);
        assertThat(runCommandBuilder.getLinks(), Matchers.<Map.Entry<String,String>>containsInAnyOrder(new AbstractMap.SimpleEntry<>("link1", "link1"),
            new AbstractMap.SimpleEntry<>("link2", "link2")));
    }

    @Test
    public void specialProperties() throws IOException {
        String propertiesString = ""
            + "name=my-container\n"
            + "user=root\n"
            + "workDir=/tmp\n"
            + "entryPoint=/bin/bash";

        RunCommandBuilder runCommandBuilder = new RunCommandBuilder(propertiesString);
        assertEquals("my-container", runCommandBuilder.getName());
        assertEquals("root", runCommandBuilder.getUser());
        assertEquals("/tmp", runCommandBuilder.getWorkDir());
        assertEquals("/bin/bash", runCommandBuilder.getEntryPoint());
    }

    @Test
    public void getCommandString() throws IOException {
        String propertiesString = ""
            + "name=my-container\n"
            + "user=root\n"
            + "workDir=/tmp\n"
            + "entryPoint=/bin/bash\n"
            + "link.link1=link1\n"
            + "port.8080=8080\n"
            + "vol./export/data=/vol1\n"
            + "env.rg.config.test=var\n";

        RunCommandBuilder runCommandBuilder = new RunCommandBuilder(propertiesString);
        assertEquals("docker run \\\n"
            + "--name 'my-container' \\\n"
            + "--user root \\\n"
            + "--entrypoint '/bin/bash' \\\n"
            + "--restart always \\\n"
            + "-w '/tmp' \\\n"
            + "-p 8080:8080 \\\n"
            + "-e rg.config.test='var' \\\n"
            + "-v /export/data:/vol1 \\\n"
            + "--link link1:link1 \\\n"
            + "http://custom-rating:8080/image", runCommandBuilder.toDockerRunCommand("http://custom-rating:8080/image"));
    }


}
