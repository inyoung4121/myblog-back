package in.myblog.sidebar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SidebarDataDTO {
    private VisitorCountsDTO visitorCounts;
    private List<String> tags;
}
