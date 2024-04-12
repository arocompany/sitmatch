package com.nex.download;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DownloadDto {
    private String zipFileName;
    private List<String> sourceFiles;
}
