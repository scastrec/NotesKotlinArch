package com.castrec.stephane.noteskotlinsample.notes.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.castrec.stephane.noteskotlinsample.R
import com.castrec.stephane.noteskotlinsample.di.NotesDH
import com.castrec.stephane.noteskotlinsample.notes.fragments.NotesRecyclerViewAdapter
import com.castrec.stephane.noteskotlinsample.notes.model.Note
import com.castrec.stephane.noteskotlinsample.notes.viewmodel.NotesViewModel
import com.castrec.stephane.noteskotlinsample.notes.viewmodel.NotesViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class NotesFragment : Fragment() {

    private val component by lazy { NotesDH.notesComponent() }

    @Inject
    lateinit var viewModelFactory: NotesViewModelFactory
    private val viewModel: NotesViewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(NotesViewModel::class.java) }

    private val disposable = CompositeDisposable()

    private lateinit var adapter : NotesRecyclerViewAdapter

    private lateinit var mV:RecyclerView


    private var mColumnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            if (mColumnCount <= 1) {
                view.layoutManager = LinearLayoutManager(context)
            } else {
                view.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            mV = view
        }

        component.inject(this)


        return view
    }


    override fun onStart() {
        super.onStart()
        disposable.add(viewModel.fetchNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ notes: List<Note> -> updateNotes(notes) },
                        { error -> manageError(error) }))
    }

    private fun updateNotes(notes: List<Note>) {
        adapter = NotesRecyclerViewAdapter(notes)
        if (mV is RecyclerView) {
            //Shitty. Have to find a proper way to update adapter
            adapter = NotesRecyclerViewAdapter(notes)
            mV.adapter = adapter
        }
    }

    private fun manageError(error: Throwable?) {
        Toast.makeText(context, context?.getString(R.string.notes_error), Toast.LENGTH_SHORT).show()
    }

    override fun onDetach() {
        super.onDetach()
        disposable.clear()
    }

    companion object {

        fun newInstance(): NotesFragment {
            return NotesFragment()
        }
    }
}
