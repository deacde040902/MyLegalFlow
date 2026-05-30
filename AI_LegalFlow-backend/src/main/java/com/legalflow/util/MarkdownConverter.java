package com.legalflow.util;

public class MarkdownConverter {

    public static String toHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body>");

        String[] lines = markdown.split("\n");
        boolean inCodeBlock = false;

        for (String line : lines) {
            if (line.startsWith("```")) {
                if (!inCodeBlock) {
                    html.append("<pre><code>");
                    inCodeBlock = true;
                } else {
                    html.append("</code></pre>");
                    inCodeBlock = false;
                }
                continue;
            }

            if (inCodeBlock) {
                html.append(escapeHtml(line)).append("<br/>");
                continue;
            }

            if (line.startsWith("# ")) {
                html.append("<h1>").append(escapeHtml(line.substring(2))).append("</h1>");
            } else if (line.startsWith("## ")) {
                html.append("<h2>").append(escapeHtml(line.substring(3))).append("</h2>");
            } else if (line.startsWith("### ")) {
                html.append("<h3>").append(escapeHtml(line.substring(4))).append("</h3>");
            } else if (line.startsWith("- ")) {
                html.append("<li>").append(escapeHtml(line.substring(2))).append("</li>");
            } else if (line.startsWith("**") && line.endsWith("**")) {
                html.append("<p><strong>").append(escapeHtml(line.substring(2, line.length() - 2))).append("</strong></p>");
            } else if (!line.trim().isEmpty()) {
                html.append("<p>").append(escapeHtml(line)).append("</p>");
            }
        }

        html.append("</body></html>");
        return html.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
