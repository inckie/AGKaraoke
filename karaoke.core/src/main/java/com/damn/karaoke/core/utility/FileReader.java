package com.damn.karaoke.core.utility;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FileReader {

    public static List<String> readLines(File file) throws IOException {
        try(BOMInputStream bomInputStream = new BOMInputStream(new FileInputStream(file))) {
            ByteOrderMark bom = bomInputStream.getBOM();
            String charsetName = bom == null ? "UTF-8" : bom.getCharsetName();
            return IOUtils.readLines(bomInputStream, charsetName);
        }
    }
}
