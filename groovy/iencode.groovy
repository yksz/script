#!/usr/bin/env groovy
/**
 * Encode image to Base64 string
 */

import javax.imageio.ImageIO

def encodeToBase64(image, format) {
    def output = new ByteArrayOutputStream()
    ImageIO.write(image, format, output)
    def bytes = output.toByteArray()
    output.close()
    return bytes.encodeBase64().toString()
}

def getExtension(filepath) {
    def index = filepath.lastIndexOf('.')
    if (index == -1)
        throw new Exception("Extension not found: $filepath")
    return filepath.substring(index + 1)
}


if (args.length < 1) {
    println 'usage: iencode.groovy <filepath>'
    System.exit(1)
}
def filepath = args[0]
def image = ImageIO.read(new File(filepath))
def format = getExtension(filepath)
println encodeToBase64(image, format)
