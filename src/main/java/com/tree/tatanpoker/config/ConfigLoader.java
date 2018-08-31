package com.tree.tatanpoker.config;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;

public interface ConfigLoader {
    void loadConfig(YamlReader reader);
}
