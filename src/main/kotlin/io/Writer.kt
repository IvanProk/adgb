package io

import java.io.*

class Writer(private val file: File) {

    fun write(dependencies: Map<String, Set<String>>) {
        try {
            BufferedWriter(FileWriter(file)).use { br ->
                br.write("var dependencies = {links:[\n")
                for ((key, value) in dependencies) {
                    //System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue().toArray(new String[entry.getValue().size()])));
                    for (dep in value) {
                        br.write("{\"source\":\"$key\",\"dest\":\"$dep\"},\n")
                    }
                }
                br.write("]};")
            }
        } catch (e: FileNotFoundException) {
            System.err.println("Cannot found " + file.absolutePath)
        } catch (e: IOException) {
            System.err.println("Cannot write " + file.absolutePath)
        }

    }

}