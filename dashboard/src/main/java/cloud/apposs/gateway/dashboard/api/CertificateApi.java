package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CertificateDef;
import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.CertificateModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.global.certificate.CertificateConstant;
import cloud.apposs.gateway.util.CertificateUtil;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Model;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.rest.annotation.Variable;
import cloud.apposs.util.Errno;
import cloud.apposs.util.Param;
import cloud.apposs.util.StandardResult;
import cloud.apposs.util.Table;

import java.security.cert.X509Certificate;
import java.util.List;

@RestAction
public class CertificateApi {
    @Autowired
    private INode node;

    @Request.Get("/api/v1/gateway/certificate/{id}")
    public React<StandardResult> getCertificateInfo(@Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getCertificateInfo(id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Certificate " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    @Request.Get("/api/v1/gateway/certificates")
    @Permission("system:setting:certificate:list")
    public React<StandardResult> getCertificateList() {
        return React.emitter(() -> {
            Table<Param> dataList = node.getCertificateList();
            if (dataList == null) {
                dataList = Table.builder();
            }
            // 解析证书信息
            for (Param data : dataList) {
                handleDataConvert(data);
            }
            return StandardResult.success(dataList, false);
        });
    }

    @Request.Put("/api/v1/gateway/certificate")
    @Permission("system:setting:certificate:add")
    @OperationLog(type = OperationType.ADD, module = "certificate")
    public React<StandardResult> addCertificate(@Model CertificateModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(CertificateConstant.KEY_ID, id)
                    .setString(CertificateConstant.KEY_DOMAIN, request.getDomain())
                    .setString(CertificateConstant.KEY_KEY_DATA, request.getKeyData())
                    .setString(CertificateConstant.KEY_CERT_DATA, request.getCertData())
                    .setString(CertificateConstant.KEY_REMARK, request.getRemark());
            boolean success = node.addCertificate(id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Post("/api/v1/gateway/certificate/{id}")
    @Permission("system:setting:certificate:edit")
    @OperationLog(type = OperationType.UPDATE, module = "certificate")
    public React<StandardResult> updateCertificate(@Variable("id") String id, @Model CertificateModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(CertificateConstant.KEY_ID, id)
                    .setString(CertificateConstant.KEY_DOMAIN, request.getDomain())
                    .setString(CertificateConstant.KEY_KEY_DATA, request.getKeyData())
                    .setString(CertificateConstant.KEY_CERT_DATA, request.getCertData())
                    .setString(CertificateConstant.KEY_REMARK, request.getRemark());
            boolean success = node.updateCertificate(request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Delete("/api/v1/gateway/certificate/{id}")
    @Permission("system:setting:certificate:delete")
    @OperationLog(type = OperationType.DELETE, module = "certificate")
    public React<StandardResult> deleteCertificate(@Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.deleteCertificate(id);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    private void handleDataConvert(Param infomation) throws Exception {
        String content = infomation.getString(CertificateConstant.KEY_CERT_DATA);
        List<X509Certificate> certificates = CertificateUtil.loadCertificates(content);
        infomation.remove(CertificateConstant.KEY_CERT_DATA);
        infomation.remove(CertificateConstant.KEY_KEY_DATA);
        if (certificates == null || certificates.isEmpty()) {
            return;
        }
        X509Certificate certificate = certificates.get(0);
        infomation.setLong(CertificateDef.Info.BEFORE, certificate.getNotBefore().getTime());
        infomation.setLong(CertificateDef.Info.AFTER, certificate.getNotAfter().getTime());
        infomation.setString(CertificateDef.Info.ISSUER, certificate.getIssuerX500Principal().getName());
    }
}
