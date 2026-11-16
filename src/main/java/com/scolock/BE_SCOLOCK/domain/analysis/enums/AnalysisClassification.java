package com.scolock.BE_SCOLOCK.domain.analysis.enums;

public enum AnalysisClassification {
    MISSING_INFO, // 제작 반영에 필요한 정보가 빠짐
    AMBIGUOUS, //여러의미로 해석 가능
    BASELINE_CHANGE, // 기존 제작 기준과 달라질 가능성
    TECH_CHECK // 본제작 전에 구현 방식/테스트 확인이 필요한 유형
}