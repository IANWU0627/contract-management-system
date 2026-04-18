import Quill from 'quill'

const BlockEmbed = Quill.import('blots/block/embed') as any

/**
 * Wraps raw HTML tables so Quill keeps them in the document model (default clipboard strips &lt;table&gt;).
 */
class TableEmbed extends BlockEmbed {
  static blotName = 'table-embed'
  static tagName = 'div'
  static className = 'ql-table-embed'

  static create(value: string) {
    const node = super.create() as HTMLElement
    if (typeof value === 'string') {
      node.innerHTML = value
    }
    return node
  }

  static value(node: HTMLElement) {
    return node.innerHTML
  }
}

let registered = false

export function registerQuillTableEmbed() {
  if (registered) return
  Quill.register(TableEmbed, true)
  registered = true
}

registerQuillTableEmbed()
