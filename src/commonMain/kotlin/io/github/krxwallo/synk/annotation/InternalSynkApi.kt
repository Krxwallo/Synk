package io.github.krxwallo.synk.annotation

@RequiresOptIn(message = "This is part of the internal Synk API that is not exposed for public use.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class InternalSynkApi