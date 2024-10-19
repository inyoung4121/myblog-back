package in.myblog.sidebar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VisitorCountsDTO {
    private long total;
    private long today;
    private long yesterday;
}
