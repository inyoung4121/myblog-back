package in.myblog.user.repository;


import in.myblog.user.domain.RoleChangeRequest;
import in.myblog.user.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {
    boolean existsByUserAndStatus(Users user, RoleChangeRequest.RequestStatus status);

}