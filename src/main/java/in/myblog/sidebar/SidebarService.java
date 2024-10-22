package in.myblog.sidebar;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SidebarService {

    private final SidebarCacheService sidebarCacheService;

    public SidebarDataDTO getSidebarData() {
        VisitorCountsDTO visitorCounts = sidebarCacheService.getVisitorCounts();
        List<String> tags = sidebarCacheService.getTags();

        return new SidebarDataDTO(visitorCounts, tags);
    }


}