package fr.geonature.occtax

import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Helper functions about loading fixtures files from resources `fixtures/` folder.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object FixtureHelper {

    /**
     * Reads the contents of a file as `File`.
     *
     * @param name the file to read (e.g. XML, JSON or any text file), must not be `null`
     *
     * @return the contents as `File`
     */
    @Throws(FileNotFoundException::class)
    fun getFixtureAsFile(name: String): File {
        val resource = FixtureHelper::class.java.classLoader!!.getResource("fixtures/$name")
            ?: throw FileNotFoundException("file not found $name")

        return File(resource.file)
    }

    /**
     * Reads the contents of a file as string.
     * The file is always closed.
     *
     * @param name the file to read (e.g. XML, JSON or any text file), must not be `null`
     *
     * @return the file contents, never `null`
     */
    fun getFixture(name: String): String {
        val stringBuilder = StringBuilder()

        val inputStream = getFixtureAsStream(name) ?: return stringBuilder.toString()

        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        try {
            var line: String? = bufferedReader.readLine()

            while (line != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n")
                line = bufferedReader.readLine()
            }
        } catch (ignored: IOException) {
        } finally {
            try {
                bufferedReader.close()
            } catch (ignored: IOException) {
            }
        }

        return stringBuilder.toString().trim { it <= ' ' }
    }

    /**
     * Reads the contents of a file as [InputStream].
     *
     * @param name the file to read (e.g. XML, JSON or any text file), must not be `null`
     *
     * @return the file contents as [InputStream]
     */
    private fun getFixtureAsStream(name: String): InputStream? {
        return FixtureHelper::class.java.classLoader!!.getResourceAsStream("fixtures/$name")
    }
}
