package com.spring.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.spring.dto.request.VisaRecordRequestDto;
import com.spring.entity.Admin;
import com.spring.entity.ApprovalStatus;
import com.spring.entity.VisaRecord;
import com.spring.repository.AdminRepository;
import com.spring.repository.VisaRecordRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VisaRecordServiceImpl implements VisaRecordService {

    private final VisaRecordRepository visaRecordRepository;
    private final AdminRepository adminRepository;

    /**
     * (옵션) agentId를 명시적으로 받아 등록하는 케이스.
     * - 실제 컨트롤러 플로우는 register(...) 사용.
     * - 여기서도 반드시 쓰기 권한(권한ID==5) 체크.
     */
    @Override
    public Long visaCreate(VisaRecordRequestDto dto, Long agentId) {
        Admin actor = requireCurrentAdmin();
        if (!hasVisaWriteRole(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "등록 권한이 없습니다. (권한 5만 가능)");
        }

        VisaRecord v = dto.toEntity();

        // agent 설정
        if (agentId != null) {
            Admin agent = adminRepository.findById(agentId).orElse(null);
            v.setAgent(agent);
        } else {
            v.setAgent(actor); // 기본: 등록자
        }

        return visaRecordRepository.save(v).getVisaId();
    }

    /** 수정 화면용 폼 데이터 조회 (읽기 전용) */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public VisaRecordRequestDto getVisaRecordForm(Long id) {
        VisaRecord v = visaRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("비자 정보가 없습니다. id=" + id));
        return VisaRecordRequestDto.fromEntity(v);
    }

    /** 일반 등록: 현재 로그인 Admin을 agent로 자동 설정 (권한 5 필수) */
    @Override
    public Long register(VisaRecordRequestDto dto) {
        Admin actor = requireCurrentAdmin();
        if (!hasVisaWriteRole(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "등록 권한이 없습니다. (권한 5만 가능)");
        }

        VisaRecord v = dto.toEntity();
        v.setAgent(actor); // 등록자=소유자
        return visaRecordRepository.save(v).getVisaId();
    }

    /** 수정: 권한ID==5만 허용 */
    @Override
    public void update(Long id, VisaRecordRequestDto dto) {
        VisaRecord v = visaRecordRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("비자 정보가 없습니다. id=" + id));

        Admin actor = requireCurrentAdmin();
        if (!hasVisaWriteRole(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다. (권한 5만 가능)");
        }

        // ✅ 본인 작성 건만 허용
        Long ownerId = (v.getAgent() != null) ? v.getAgent().getAdminId() : null;
        if (ownerId != null && !ownerId.equals(actor.getAdminId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 건만 수정할 수 있습니다.");
        }

        dto.applyToEntity(v);
        if (v.getAgent() == null) v.setAgent(actor);
    }


    /** 단건 엔티티 조회 (읽기 전용) */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public VisaRecord getEntity(Long id) {
        return visaRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("비자 정보가 없습니다. id=" + id));
    }

    /** 최근 등록 리스트 반환 (정렬: visaId DESC, 상위 limit개) */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<VisaRecord> findRecent(int limit) {
        int size = Math.max(1, limit);
        var pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "visaId"));
        return visaRecordRepository.findAll(pageable).getContent();
    }

    /** 삭제: 권한ID==5만 허용 */
    @Override
    public void delete(Long id) {
        VisaRecord v = visaRecordRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("비자 정보가 없습니다. id=" + id));

        Admin actor = requireCurrentAdmin();
        if (!hasVisaWriteRole(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다. (권한 5만 가능)");
        }

        // ✅ 본인 작성 건만 허용
        Long ownerId = (v.getAgent() != null) ? v.getAgent().getAdminId() : null;
        if (ownerId != null && !ownerId.equals(actor.getAdminId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 건만 삭제할 수 있습니다.");
        }

        v.setVisaDeleted(true); // 소프트 삭제
    }

    /* =========================
       권한/로그인 유틸
       ========================= */

    /** 현재 로그인한 Admin (없으면 null) */
    private Admin resolveCurrentAdminOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return null;
        }
        Object p = auth.getPrincipal();
        if (p instanceof Admin a) return a;

        String loginId = (p instanceof UserDetails ud) ? ud.getUsername() : String.valueOf(p);
        return adminRepository.findByAdminLoginId(loginId); // 없으면 null
    }

    /** 로그인 Admin이 반드시 필요할 때 */
    private Admin requireCurrentAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        Object p = auth.getPrincipal();
        if (p instanceof Admin a) return a;

        String loginId = (p instanceof UserDetails ud) ? ud.getUsername() : String.valueOf(p);
        Admin admin = adminRepository.findByAdminLoginId(loginId);
        if (admin == null) throw new SecurityException("관리자를 찾을 수 없습니다.");
        return admin;
    }

    /** ✅ VISA 쓰기 권한: 권한ID==5 만 허용 (최고권한은 읽기 전용) */
    private boolean hasVisaWriteRole(Admin admin) {
        if (admin == null || admin.getAuthorityType() == null) return false;
        Integer id = admin.getAuthorityType().getAuthorityId();
        return id != null && id == 5;
        // (선택) 이름으로도 허용하려면 아래 보조 조건 추가
        // String name = admin.getAuthorityType().getAuthorityName();
        // return (id != null && id == 5) || ("VISA_EDITOR".equalsIgnoreCase(name));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<VisaRecord> findRecentActive(int limit) {
        var pageable = PageRequest.of(0, Math.max(1, limit), Sort.by(Sort.Direction.DESC, "visaId"));
        return visaRecordRepository
        	.findByVisaDeletedFalse(pageable) 
            .getContent();
    }

    @Override
    public List<VisaRecord> findRecentIncludingDeleted(int limit) {
        return visaRecordRepository.findAll(
                    PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "visaId"))
               ).getContent();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<VisaRecord> search(String keyword, boolean includeDeleted, Pageable pageable) {
        Admin me = resolveCurrentAdminOrNull();

        // 슈퍼(1,2)면 전체 조회 → agentId=null, 권한5면 내 것만 → agentId=나의 adminId
        Long agentId = null;
        if (me != null) {
            Integer auth = (me.getAuthorityType() != null) ? me.getAuthorityType().getAuthorityId() : null;
            boolean isSuper = (auth != null) && (auth == 1 || auth == 2);
            if (!isSuper) {
                agentId = me.getAdminId(); // 권한5 등은 내 것만
            }
        }

        String kw = (keyword == null) ? "" : keyword.trim();
        ApprovalStatus kwStatus = mapToApprovalStatus(kw);

        // ✅ 키워드가 비어도 이 경로 하나로 통일 (쿼리 내부에 ":kw = '' or ..." 조건이 이미 있음)
        return visaRecordRepository.search(kw, kwStatus, includeDeleted, agentId, pageable);
    }

    // ✅ 에이전트 전용: 내 것만
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<VisaRecord> searchMine(String keyword, boolean includeDeleted, Pageable pageable) {
        Admin me = requireCurrentAdmin();
        String kw = (keyword == null) ? "" : keyword.trim();
        ApprovalStatus kwStatus = mapToApprovalStatus(kw);
        return visaRecordRepository.search(kw, kwStatus, includeDeleted, me.getAdminId(), pageable);
    }

    private ApprovalStatus mapToApprovalStatus(String kw) {
        if (kw == null) return null;
        String s = kw.trim().toLowerCase();

        // 1) enum 이름/라벨 직접 매칭
        for (ApprovalStatus st : ApprovalStatus.values()) {
            if (st.name().equalsIgnoreCase(kw)) return st;
            String label = st.label();
            if (label != null && label.equalsIgnoreCase(kw)) return st;
        }

        // 2) 한글/영문 유사어 매핑
        if (s.contains("승인") || s.contains("허가") || s.equals("approved")) return ApprovalStatus.APPROVED;
        if (s.contains("불허") || s.contains("반려") || s.contains("거절") || s.equals("rejected")) return ApprovalStatus.REJECTED;
        if (s.contains("접수")) return ApprovalStatus.RECEIVED;                 // 비자접수중
        if (s.contains("보류") || s.contains("대기") || s.equals("pending") || s.equals("hold"))
            return ApprovalStatus.HOLD;                                         // 보류/대기

        return null; // 상태 매칭이 아니면 일반 텍스트 검색만
    }
}
