package dev.galasa.staging;

import java.util.List;

import dev.galasa.staging.json.Asset;

public class Artifact implements Comparable<Artifact> {
    
    public String id;
    public String group;
    public String artifact;
    public String version;
    public String repoId;
    public List<Asset> assets;
    
    @Override
    public int compareTo(Artifact o) {
        int compare = group.compareTo(o.group);
        if (compare != 0) {
            return compare;
        }

        compare = artifact.compareTo(o.artifact);
        if (compare != 0) {
            return compare;
        }

        compare = version.compareTo(o.version);
        if (compare != 0) {
            return compare;
        }
        
        return 0;
    }


    public String getName() {
        return this.group + ":" + this.artifact;
    }


    public String getNameVersion() {
        return this.group + ":" + this.artifact + ":" + this.version;
    }

}
