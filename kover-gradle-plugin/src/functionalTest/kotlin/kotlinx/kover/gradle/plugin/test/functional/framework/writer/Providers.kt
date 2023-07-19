package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File
import java.util.function.BiFunction

internal class PropertyWriter<T : Any>(
    private val propertyName: String,
    private val writer: FormattedWriter
) : Property<T> {

    override fun get(): T {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getOrNull(): T? {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun isPresent(): Boolean {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun forUseAtConfigurationTime(): Provider<T> {
        writer.call("$propertyName.forUseAtConfigurationTime")
        return this
    }

    override fun finalizeValue() {
        writer.call("$propertyName.finalizeValue")
    }

    override fun finalizeValueOnRead() {
        writer.call("$propertyName.finalizeValueOnRead")
    }

    override fun disallowChanges() {
        writer.call("$propertyName.disallowChanges")
    }

    override fun disallowUnsafeRead() {
        writer.call("$propertyName.disallowUnsafeRead")
    }

    override fun convention(provider: Provider<out T>): Property<T> {
        writer.call("$propertyName.convention", provider.toString())
        return this
    }

    override fun convention(value: T?): Property<T> {
        writer.call("$propertyName.convention", formatForProperty(value))
        return this
    }

    override fun value(provider: Provider<out T>): Property<T> {
        writer.call("$propertyName.provider", provider.toString())
        return this
    }

    override fun value(value: T?): Property<T> {
        writer.call("$propertyName.value", formatForProperty(value))
        return this
    }

    override fun set(provider: Provider<out T>) {
        writer.call("$propertyName.set", provider.toString())
    }

    override fun set(value: T?) {
        writer.call("$propertyName.set", formatForProperty(value))
    }

    override fun <U : Any?, R : Any?> zip(right: Provider<U>, combiner: BiFunction<in T, in U, out R?>): Provider<R> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun orElse(provider: Provider<out T>): Provider<T> {
        writer.call("$propertyName.orElse", provider.toString())
        return this
    }

    override fun orElse(value: T): Provider<T> {
        writer.call("$propertyName.orElse", formatForProperty(value))
        return this
    }

    override fun <S : Any?> flatMap(transformer: Transformer<out Provider<out S>?, in T>): Provider<S> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun <S : Any?> map(transformer: Transformer<out S?, in T>): Provider<S> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getOrElse(defaultValue: T): T {
        throw KoverCriticalException("Operation not supported in functional test")
    }
}

internal class FilePropertyWriter(
    private val propertyName: String,
    private val writer: FormattedWriter
) : RegularFileProperty {
    override fun get(): RegularFile {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getOrNull(): RegularFile? {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun getOrElse(defaultValue: RegularFile): RegularFile {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun <S : Any?> map(transformer: Transformer<out S?, in RegularFile>): Provider<S> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun <S : Any?> flatMap(transformer: Transformer<out Provider<out S>?, in RegularFile>): Provider<S> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun isPresent(): Boolean {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun orElse(value: RegularFile): Provider<RegularFile> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun orElse(provider: Provider<out RegularFile>): Provider<RegularFile> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun forUseAtConfigurationTime(): Provider<RegularFile> {
        TODO("Not yet implemented")
    }

    override fun <U : Any?, R : Any?> zip(
        right: Provider<U>,
        combiner: BiFunction<in RegularFile, in U, out R?>
    ): Provider<R> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun finalizeValue() {
        TODO("Not yet implemented")
    }

    override fun finalizeValueOnRead() {
        TODO("Not yet implemented")
    }

    override fun disallowChanges() {
        TODO("Not yet implemented")
    }

    override fun disallowUnsafeRead() {
        TODO("Not yet implemented")
    }

    override fun set(file: File?) {
        writer.call("$propertyName.set", formatForProperty(file))
    }

    override fun set(value: RegularFile?) {
        writer.call("$propertyName.set", value.toString())
    }

    override fun set(provider: Provider<out RegularFile>) {
        writer.call("$propertyName.set", provider.toString())
    }

    override fun value(value: RegularFile?): RegularFileProperty {
        writer.call("$propertyName.value", value.toString())
        return this
    }

    override fun value(provider: Provider<out RegularFile>): RegularFileProperty {
        writer.call("$propertyName.value", provider.toString())
        return this
    }

    override fun convention(value: RegularFile?): RegularFileProperty {
        writer.call("$propertyName.convention", value.toString())
        return this
    }

    override fun convention(provider: Provider<out RegularFile>): RegularFileProperty {
        writer.call("$propertyName.convention", provider.toString())
        return this
    }

    override fun getAsFile(): Provider<File> {
        throw KoverCriticalException("Operation not supported in functional test")
    }

    override fun fileValue(file: File?): RegularFileProperty {
        writer.call("$propertyName.fileValue", formatForProperty(file))
        return this
    }

    override fun fileProvider(provider: Provider<File>): RegularFileProperty {
        writer.call("$propertyName.fileProvider", provider.toString())
        return this
    }

    override fun getLocationOnly(): Provider<RegularFile> {
        writer.call("$propertyName.getLocationOnly")
        return this
    }

}

private fun <T> formatForProperty(value: T): String {
    return when (value) {
        null -> "null"
        is String -> "\"$value\""
        is File -> "file(\"$value\")"
        else -> value.toString()
    }
}