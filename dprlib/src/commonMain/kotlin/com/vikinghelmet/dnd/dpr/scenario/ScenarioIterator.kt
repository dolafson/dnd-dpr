package com.vikinghelmet.dnd.dpr.scenario

import dev.shivathapaa.logger.api.LoggerFactory

class ScenarioIterator(scenarioList: List<Scenario>) : Iterator<Scenario>
{
    val logger = LoggerFactory.get(ScenarioIterator::class.simpleName ?: "no simpleName")

    val iterator: Iterator<Scenario>
    var size: Int = 0
    var completed: Int = 0

    init {
        iterator = scenarioList.listIterator()
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): Scenario {
        completed++
        return iterator.next()
    }

    fun getPercentComplete(): Float {
        return if (!iterator.hasNext()) { 100.0f } else { (completed * 1.0f) / (size * 1.0f) }
    }
}
