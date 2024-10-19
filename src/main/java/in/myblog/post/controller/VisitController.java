package in.myblog.post.controller;

import in.myblog.post.service.VisitServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class VisitController {
    private final VisitServiceImpl visitService;

    @PostMapping("/visit")
    public ResponseEntity<Void> trackVisit() {
        visitService.incrementVisitCount();
        return ResponseEntity.ok().build();
    }
}
