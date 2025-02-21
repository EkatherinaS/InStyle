package mse.instyle;

public class Templates {
    private static final String templateCss = """
            /*default*/
            :root {
                /*variables*/
                {variables}
                        
                --rs-font-family-ui: var(--default-font-family);
                --rs-font-family-headers: var(--default-font-family);
                --rs-font-family-mono: var(--default-font-family);
                        
                --wh-color-text-heading: var(--default-color);
                --wh-color-text-main: var(--default-color);
                --wh-color-text-secondary: var(--default-color);
                --wh-color-text-pale: var(--default-color);
                        
                --rs-text-2-font-size: var(--default-font-size);
                --rs-text-2-font-weight: var(--default-font-weight);
                --rs-text-2-line-height: var(--default-line-height);
            }
            body {
                * {
                    font-weight: var(--default-font-weight);
                    font-family: var(--default-font-family);
                    font-size: var(--default-font-size);
                    line-height: var(--default-line-height);
                    color: var(--default-color);
                        
                    text-align: var(--default-text-align);
                    text-transform: var(--default-text-transform);
                    text-indent: var(--default-text-indent);
                        
                    padding: 0 !important;
                }
            }
            .article {
                text-align: var(--default-text-align);
            }
                        
            a {
                text-indent: 0mm !important;
            }
                        
            .code-block code {
                text-indent: 0mm !important;
            }
                        
            table .article__p {
                text-indent: 0mm !important;
            }
                        
            .keystroke {
                text-indent: 0mm !important;
            }
                        
            svg .article__p {
                text-indent: 0mm !important;
            }
                        
            /*
            img-text: {
                font-size: 11pt;
                text-align: center;
                font-weight: italic bold;
                template: "Рисунок {number} - {caption}"
            },
            table-text: {
                template: "Таблица {number} - {caption}"
            },*/
            """;
    private static final String templateHeaderCss = """
            {num} {
                text-align: var(--{num}-text-align, --default-text-align);
                text-indent: var(--{num}-text-indent, --default-text-indent);
                text-transform: var(--{num}-text-transform, --default-text-transform);
                line-height: var(--{num}-line-height, --default-line-height);
                default-color: var(--{num}-default-color, --default-default-color);
                .title__content {
                    font-family: var(--{num}-font-family, --default-font-family);
                    font-weight: var(--{num}-font-weight, --default-font-weight);
                    font-size: var(--{num}-font-size, --default-font-size);
                }
                /*start_new_page: true;*/
            }
            """;

    private static final String contentsCss = """
                    .row {
                        display: -webkit-box;
                        -webkit-box-pack: justify;
                        width: 100%;
                    }
                    .left {
                        -webkit-box-flex: 0;
                        white-space: nowrap;
                    }
                    .separator {
                        -webkit-box-flex: 1;
                        position: relative;
                        overflow: hidden;
                    }
                    .separator::after {
                        content: "{separator}";
                        white-space: nowrap;
                        display: block;
                        overflow: hidden;
                        width: 100%;
                    }
                    .right {
                        -webkit-box-flex: 0;
                        white-space: nowrap;
                    }
            """;

    public static String getTemplateCss() {
        return templateCss;
    }

    public static String getTemplateHeaderCss() {
        return templateHeaderCss;
    }

    public static String getContentsCss() {
        return contentsCss;
    }
}
