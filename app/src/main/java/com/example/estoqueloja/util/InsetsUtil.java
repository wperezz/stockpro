package com.example.estoqueloja.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetsUtil {

    // Guarda o padding original no próprio View (tag)
    private static final int TAG_INITIAL_PADDING = 0x7f0b0001; // id "fake" estável

    private static class InitialPadding {
        final int left, top, right, bottom;
        InitialPadding(int l, int t, int r, int b) {
            left = l; top = t; right = r; bottom = b;
        }
    }

    /**
     * Recomendado para a maioria das telas:
     * - adiciona insets SOMENTE em TOP/BOTTOM
     * - mantém o padding lateral original (evita "corte" e variação de largura entre aparelhos)
     */
    public static void applyPaddingSystemBarsTopBottom(View root) {
        applyPaddingSystemBars(root, false);
    }

    /**
     * Use somente se você realmente quer respeitar insets laterais (edge-to-edge real).
     * Em alguns aparelhos isso muda largura e pode "apertar" conteúdo.
     */
    public static void applyPaddingSystemBarsAllSides(View root) {
        applyPaddingSystemBars(root, true);
    }

    private static void applyPaddingSystemBars(View root, boolean includeHorizontal) {

        // salva padding original uma única vez
        if (root.getTag(TAG_INITIAL_PADDING) == null) {
            root.setTag(TAG_INITIAL_PADDING,
                    new InitialPadding(root.getPaddingLeft(), root.getPaddingTop(),
                            root.getPaddingRight(), root.getPaddingBottom()));
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            InitialPadding p = (InitialPadding) v.getTag(TAG_INITIAL_PADDING);
            if (p == null) p = new InitialPadding(0,0,0,0);

            int left = p.left + (includeHorizontal ? sys.left : 0);
            int right = p.right + (includeHorizontal ? sys.right : 0);
            int top = p.top + sys.top;
            int bottom = p.bottom + sys.bottom;

            v.setPadding(left, top, right, bottom);
            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }
}
