package zelifkudos

import org.springframework.core.io.ClassPathResource

class PwaController {

    def manifest() {
        def resource = new ClassPathResource("static/manifest.json")
        response.contentType = "application/manifest+json"
        response.outputStream << resource.inputStream
        response.outputStream.flush()
    }

    def serviceWorker() {
        def resource = new ClassPathResource("static/service-worker.js")
        response.contentType = "application/javascript"
        response.outputStream << resource.inputStream
        response.outputStream.flush()
    }

    def icon() {
        String filename = params.filename
        def resource = new ClassPathResource("static/icons/${filename}")
        if (!resource.exists()) {
            render status: 404
            return
        }
        response.contentType = "image/png"
        response.outputStream << resource.inputStream
        response.outputStream.flush()
    }
}
