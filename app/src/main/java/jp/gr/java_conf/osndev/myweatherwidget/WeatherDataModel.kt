package jp.gr.java_conf.osndev.myweatherwidget

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class WeatherDataModel {

    var weather: String = ""
    var temperature: String = ""
    var link: String = ""

    fun parse(data: String) {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(data))

        var isItem = false
        var eventType = parser.eventType
        var tag: String = ""
        parse@while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                tag = parser.name
                when (tag) {
                    "item" -> isItem = true
                }
            } else if (eventType == XmlPullParser.TEXT) {
                when (tag) {
                    "link" -> if (isItem) {
                        this.link = parser.text
                    }
                    "description" -> if (isItem) {
                        this.parseWeather(parser.text)
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                // item１件分だけ読み込む
                if (parser.name == "item") {
                    break
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseWeather(data: String) {
        var d = data.split(" - ")
        if (2 != d.size) {
            this.weather = "N/A"
            this.temperature = "-/-"
        } else {
            this.weather = d[0]
            this.temperature = d[1]
        }
    }
}