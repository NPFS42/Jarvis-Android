package net.sovereignresearch.jarvis

/**
 * The search picker presents only independently verified nodes. Selection
 * populates the editable host field; it never implicitly opens a connection.
 */
object JarvisNodePicker {
    fun choices(nodes: List<TailnetClient.Node>): List<TailnetClient.Node> =
        nodes.distinctBy { it.host }.sortedBy { it.host }

    fun selectedHost(nodes: List<TailnetClient.Node>, position: Int): String? =
        choices(nodes).getOrNull(position)?.host
}
