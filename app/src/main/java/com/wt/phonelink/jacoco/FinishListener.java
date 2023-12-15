package com.wt.phonelink.jacoco;

public interface FinishListener {
    void onActivityFinished();
    void dumpIntermediateCoverage(String filePath);
}