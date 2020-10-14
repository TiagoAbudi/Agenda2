package br.com.alura.agenda.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import br.com.alura.agenda.R;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.ui.helper.FormularioHelper;

public class FormularioActivity extends AppCompatActivity {

    public static final int CODIGO_CAMERA = 567;
    private final Intent abreCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    private FormularioHelper helper;
    private String caminhoFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        helper = new FormularioHelper(this);

        Intent intent = getIntent();
        Aluno aluno = (Aluno) intent.getSerializableExtra("aluno");
        if (aluno != null) {
            helper.preencheFormulario(aluno);
        }

        configuraBotaoFoto();
        validaPermissaoCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODIGO_CAMERA) {
                helper.carregaImagem(caminhoFoto);
            }
        }
    }

    private void configuraBotaoFoto() {
        Button botaoFoto = findViewById(R.id.formulario_botao_foto);
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caminhoFoto = getExternalFilesDir(null) + "/" + System.currentTimeMillis() + ".jpeg";
                File arquivoFoto = new File(caminhoFoto);
                abreCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto));
                startActivityForResult(abreCamera, CODIGO_CAMERA);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_formulario, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_formulario_ok) {
            Aluno aluno = helper.pegaAluno();

            AlunoDAO dao = new AlunoDAO(this);
            if (aluno.getId() != null) {
                dao.altera(aluno);
            } else {
                dao.insere(aluno);
            }
            dao.close();

            Toast.makeText(FormularioActivity.this, "Aluno " + aluno.getNome() + " salvo!", Toast.LENGTH_SHORT).show();

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

//    private void preencheCampoFoto() {
//        Aluno aluno = new Aluno();
//        if (!aluno.getCaminhoFoto().equals("")) {
//            ImageView foto = findViewById(R.id.formulario_foto);
//            Bitmap bitmap = BitmapFactory.decodeFile(aluno.getCaminhoFoto());
//            caminhoFoto = aluno.getCaminhoFoto();
//            Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 90, 90, true);
//            Matrix matrix = new Matrix();
//            matrix.postRotate(270);
//            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapReduzido,
//                    0,
//                    0,
//                    bitmapReduzido.getWidth(),
//                    bitmapReduzido.getHeight(),
//                    matrix,
//                    true);
//            foto.setImageBitmap(rotatedBitmap);
//        }
//    }

    public void validaPermissaoCamera() {
        int PERMISSOES_CAMERA = 0;
        if (ContextCompat.checkSelfPermission(FormularioActivity.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(FormularioActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSOES_CAMERA);
        }
    }

}