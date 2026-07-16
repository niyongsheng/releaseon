package org.releaseon.utils.parser;

import org.releaseon.domain.entity.Package;

public interface PackageParser {
    // 解析包
    public Package parse(String filePath);
}
