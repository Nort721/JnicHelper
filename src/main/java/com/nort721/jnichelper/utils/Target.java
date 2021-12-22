package com.nort721.jnichelper.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Target {
    private final String classCore;
    private final String methodName;
    private final String methodDesc;
}
