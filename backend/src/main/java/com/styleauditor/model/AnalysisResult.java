package com.styleauditor.model;

import java.util.List;

public record AnalysisResult(
        int smoothnessIndex,
        String smoothnessLabel,
        String verdict,
        Summary summary,
        List<ChunkResult> chunks
) {
}

