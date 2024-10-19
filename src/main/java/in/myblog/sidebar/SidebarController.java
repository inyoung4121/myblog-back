package in.myblog.sidebar;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SidebarController {
    private final SidebarService sidebarService;
    @GetMapping("/api*/sidebar-data")
    public ResponseEntity<SidebarDataDTO> getSidebarData() {
        SidebarDataDTO sidebarData = sidebarService.getSidebarData();
        return ResponseEntity.ok(sidebarData);
    }
}
