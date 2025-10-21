package net.potatocloud.node.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;

@UtilityClass
public class HashUtils {

    @SneakyThrows
    public String sha256(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return DigestUtils.sha256Hex(stream);
        }
    }
}
