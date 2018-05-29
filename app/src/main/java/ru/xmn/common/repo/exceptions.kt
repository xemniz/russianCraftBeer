package ru.xmn.common.repo

import io.reactivex.exceptions.CompositeException

class RepoExceptions(val list: List<Throwable>) : Exception() {
    override fun printStackTrace() {
        list.forEach {
            it.printStackTrace()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RepoExceptions

        if (list != other.list) return false

        return true
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    companion object {
        fun from(composite: CompositeException): RepoExceptions {
            return RepoExceptions(composite.exceptions)
        }
        fun from(exceptions: List<Throwable>): RepoExceptions {
            return RepoExceptions(exceptions)
        }
    }
}