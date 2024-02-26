package kotlinx.kover.jvmagent;

import java.util.ArrayList;
import java.util.List;

public class KoverAgentSettings {
    public String reportFilePath = null;
    public boolean appendToReportFile = true;
    public List<String> inclusions = new ArrayList<String>();
    public List<String> exclusions = new ArrayList<String>();
}