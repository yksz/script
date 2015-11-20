#!/usr/bin/env groovy
/**
 * Image Cutter
 */

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

def generateCutImage(srcPath, dstPath, format, minX, minY, maxX, maxY) {
    if (!format)
        throw new IllegalArgumentException("format is '${format}'")
    if (minX >= maxX)
        throw new IllegalArgumentException("minX >= maxX")
    if (minY >= maxY)
        throw new IllegalArgumentException("minY >= maxY")
    def srcImage = ImageIO.read(new File(srcPath))
    if (minX < 0 || minX >= srcImage.width)
        throw new IllegalArgumentException("minX is invalid")
    if (minY < 0 || minY >= srcImage.height)
        throw new IllegalArgumentException("minY is invalid")
    if (maxX < 0 || maxX > srcImage.width)
        throw new IllegalArgumentException("maxX is invalid")
    if (maxY < 0 || maxY > srcImage.height)
        throw new IllegalArgumentException("maxY is invalid")
    def dstImage = cutImage(srcImage, minX, minY, maxX, maxY)
    ImageIO.write(dstImage, format, new File(dstPath))
}

def cutImage(image, minX, minY, maxX, maxY) {
    def newWidth = maxX - minX
    def newHeight = maxY - minY
    def newImage = new BufferedImage(newWidth, newHeight, image.type)
    for (y in minY..<maxY)
        for (x in minX..<maxX)
            newImage.setRGB(x - minX, y - minY, image.getRGB(x, y))
    return newImage
}

def getParentPath(filepath) {
    def str = '/'
    def index = filepath.lastIndexOf(str)
    return (index != -1) ? filepath.substring(0, index + str.size()) : ''
}

def getFileName(filepath) {
    def str = '/'
    def index = filepath.lastIndexOf(str)
    return (index != -1) ? filepath.substring(index + str.size()) : filepath
}

def getFileNameWithoutExtension(filepath) {
    def str = '.'
    def filename = getFileName(filepath)
    def index = filename.lastIndexOf(str)
    return (index != -1) ? filename.substring(0, index) : filename
}

def getExtension(filepath) {
    def str = '.'
    def index = filepath.lastIndexOf(str)
    return (index != -1) ? filepath.substring(index + str.size()) : ''
}


def cli = new CliBuilder(usage: './icut.groovy <minX> <minY> <maxX> <maxY> <image files>')
cli.with {
    _ longOpt:'prefix', args:1, 'default prefix is ""'
    _ longOpt:'suffix', args:1, 'default suffix is "_new"'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.arguments().size() < 5) {
    cli.usage()
    System.exit(1)
}
if (opt.h) {
    cli.usage()
    System.exit(0)
}
def prefix = opt.prefix ?: ''
def suffix = opt.prefix ?: '_new'
def minX = opt.arguments()[0] as int
def minY = opt.arguments()[1] as int
def maxX = opt.arguments()[2] as int
def maxY = opt.arguments()[3] as int
for (i in 4..<opt.arguments().size()) {
    def srcPath = opt.arguments()[i]
    def parentPath = getParentPath(srcPath)
    def filename = getFileNameWithoutExtension(srcPath)
    def extension = getExtension(srcPath)
    def dstPath = "${parentPath}${prefix}${filename}${suffix}.${extension}"
    generateCutImage(srcPath, dstPath, extension, minX, minY, maxX, maxY)
    println "Generate ${dstPath} from ${dstPath}"
}
