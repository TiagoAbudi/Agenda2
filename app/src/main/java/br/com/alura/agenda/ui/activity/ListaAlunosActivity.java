package br.com.alura.agenda.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import br.com.alura.agenda.R;
import br.com.alura.agenda.adapter.AlunosAdapter;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.tasks.EnviaAlunosTask;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        listaAlunos = (ListView) findViewById(R.id.lista_alunos);

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);

                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                intentVaiProFormulario.putExtra("aluno", aluno);
                startActivity(intentVaiProFormulario);
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intentVaiProFormulario);
            }
        });

        registerForContextMenu(listaAlunos);
        validaPermissaoArquivos();
    }

    private void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();

        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_enviar_notas:
                new EnviaAlunosTask(this).execute();
                break;
            case R.id.menu_baixar_provas:
                Intent vaiParaProvas = new Intent(this, ProvasActivity.class);
                startActivity(vaiParaProvas);
                break;
            case R.id.menu_mapa:
                Intent vaiParaMapa = new Intent(this, MapaActivity.class);
                startActivity(vaiParaMapa);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);

        MenuItem itemLigar = menu.add("Ligar");
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!validaPermissaoTelefone()) {
                    validaPermissaoTelefone();
                } else {
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intentLigar);
                }
                return false;
            }
        });

        MenuItem itemSMS = menu.add("Enviar SMS");
        Intent intentSMS = new Intent(Intent.ACTION_VIEW);
        intentSMS.setData(Uri.parse("sms:" + aluno.getTelefone()));
        itemSMS.setIntent(intentSMS);

        MenuItem itemMapa = menu.add("Visualizar no mapa");
        validaPermissaoLocation();

        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        intentMapa.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        itemMapa.setIntent(intentMapa);

        MenuItem itemSite = menu.add("Visitar site");
        validaPermissaoTelefone();
        Intent intentSite = new Intent(Intent.ACTION_VIEW);

        String site = aluno.getSite();
        if (!site.startsWith("http://")) {
            site = "http://" + site;
        }

        intentSite.setData(Uri.parse(site));
        itemSite.setIntent(intentSite);

        MenuItem deletar = menu.add("Deletar");
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.deleta(aluno);
                dao.close();

                carregaLista();
                return false;
            }
        });
    }

    public boolean validaPermissaoArquivos() {
        int PERMISSOES_LER = 0;
        int PERMISSOES_ESCREVER = 0;
        if (ContextCompat.checkSelfPermission(ListaAlunosActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSOES_LER);
            if (ContextCompat.checkSelfPermission(ListaAlunosActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSOES_ESCREVER);
            }
            return true;
        }
        return false;
    }


    public boolean validaPermissaoInternet() {
        int PERMISSOES_INTERNET = 0;
        if (ContextCompat.checkSelfPermission(ListaAlunosActivity.this,
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                    new String[]{Manifest.permission.INTERNET},
                    PERMISSOES_INTERNET);
            return true;
        }
        return false;
    }

    public boolean validaPermissaoLocation() {
        int PERMISSOES_FINE = 0;
        int PERMISSOES_COARSE = 0;
        if (ContextCompat.checkSelfPermission(ListaAlunosActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSOES_FINE);
            if (ContextCompat.checkSelfPermission(ListaAlunosActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSOES_COARSE);
            }
            return true;
        }
        return false;
    }

    public boolean validaPermissaoTelefone() {
        int PERMISSOES_CALL = 0;
        int PERMISSOES_SMS = 0;
        if (ContextCompat.checkSelfPermission(ListaAlunosActivity.this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PERMISSOES_CALL);
            if (ContextCompat.checkSelfPermission(ListaAlunosActivity.this,
                    Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                        new String[]{Manifest.permission.RECEIVE_SMS},
                        PERMISSOES_SMS);
            }
            return true;
        }
        return false;
    }

}
