import Markdown from 'react-native-markdown-display'
import { useMemo } from 'react'
import { StyleSheet } from 'react-native'

export interface AssistantMarkdownProps {
    markdown: string
    /** Default matches app COLORS.dark */
    textColor?: string
    linkColor?: string
}

/**
 * Renders assistant / AI reply text as Markdown (headings, lists, code, links).
 */
export function AssistantMarkdown({
    markdown,
    textColor = '#1D3557',
    linkColor = '#97B08A',
}: AssistantMarkdownProps) {
    const mdStyles = useMemo(
        () =>
            StyleSheet.create({
                body: {
                    color: textColor,
                    fontSize: 15,
                    lineHeight: 22,
                },
                paragraph: {
                    marginTop: 0,
                    marginBottom: 6,
                    flexWrap: 'wrap',
                    flexDirection: 'row',
                    width: '100%',
                },
                heading1: {
                    color: textColor,
                    fontSize: 20,
                    fontWeight: '700',
                    marginBottom: 8,
                },
                heading2: {
                    color: textColor,
                    fontSize: 18,
                    fontWeight: '700',
                    marginBottom: 6,
                },
                heading3: {
                    color: textColor,
                    fontSize: 16,
                    fontWeight: '600',
                    marginBottom: 4,
                },
                link: {
                    color: linkColor,
                    textDecorationLine: 'underline',
                },
                code_inline: {
                    backgroundColor: '#E8ECF0',
                    color: textColor,
                    paddingHorizontal: 4,
                    paddingVertical: 2,
                    borderRadius: 4,
                    fontSize: 14,
                },
                fence: {
                    backgroundColor: '#F0F4F8',
                    color: textColor,
                    fontSize: 13,
                    padding: 10,
                    borderRadius: 8,
                    marginVertical: 6,
                },
                code_block: {
                    backgroundColor: '#F0F4F8',
                    color: textColor,
                    fontSize: 13,
                    padding: 10,
                    borderRadius: 8,
                    marginVertical: 6,
                },
                blockquote: {
                    borderLeftColor: linkColor,
                    backgroundColor: 'rgba(151, 176, 138, 0.12)',
                },
                bullet_list: { marginVertical: 4 },
                ordered_list: { marginVertical: 4 },
                list_item: { marginBottom: 4 },
                strong: { color: textColor, fontWeight: '700' },
                em: { color: textColor, fontStyle: 'italic' },
                table: { borderColor: '#D0D8E0', borderWidth: 1, borderRadius: 6 },
                th: { color: textColor, fontWeight: '600', padding: 6 },
                td: { color: textColor, padding: 6 },
            }),
        [textColor, linkColor]
    )

    return (
        <Markdown mergeStyle style={mdStyles}>
            {markdown}
        </Markdown>
    )
}
