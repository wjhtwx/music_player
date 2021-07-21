package player;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

class exit extends WindowAdapter
{
	@Override
	public void windowClosing(WindowEvent event)
	{
		System.exit(0);
	}
}

class wav extends Thread
{
	public String path="audio.wav";
	public AudioFormat format=null;
	public AudioInputStream aistream=null;
	public float sampleRate=0;
	public float framelength=0;
	public float seclen=0;
	public DataLine.Info datalineinfo=null;
	public SourceDataLine dataline=null;
	public boolean pause=false;
	public boolean stop=false;
	public int played=0;
	public int play_from=0;
	public boolean pass=false;
	public wav()
	{
	}
	public void set()
	{
		try
		{
			aistream=AudioSystem.getAudioInputStream(new File(path));
			format=aistream.getFormat();
			sampleRate=format.getSampleRate();
			framelength=aistream.getFrameLength();
			seclen=framelength/sampleRate;
			datalineinfo=new DataLine.Info(SourceDataLine.class, format);
			dataline=(SourceDataLine)AudioSystem.getLine(datalineinfo);
		}
		catch(LineUnavailableException err)
		{
			System.out.println("LineUnavailableException");
		}
		catch(UnsupportedAudioFileException err)
		{
			System.out.println("UnsupportedAudioFileException");
		}
		catch(IOException err)
		{
			System.out.println("IOException");
		}
	}
	public void run()
	{
		try
		{
			byte[] bytes=new byte[512];
			int length=0;
			dataline.open(format);
			dataline.start();
			played=0;
			while(stop==false)
			{
				if(pause==false)
				{
					if((length=aistream.read(bytes))>0)
					{
						if(played>=play_from)
						{
							if(pass==false)
							{
								dataline.write(bytes, 0, length);
							}
							else
							{
								System.out.print("");
							}
							played+=1;
						}
						else
						{
							played+=1;
							System.out.print("");
						}
					}
					else
					{
						break;
					}
				}
				else
				{
					System.out.print("");
				}
			}
			stop=true;
			aistream.close();
			dataline.drain();
			dataline.close();
			
			aistream=null;
			format=null;
			sampleRate=0;
			framelength=0;
			seclen=0;
			datalineinfo=null;
			dataline=null;
			pause=false;
			stop=false;
			play_from=0;
			pass=false;
		}
		catch(Exception err)
		{
			System.out.println("Error");
			err.printStackTrace();
		}
		catch(Error err)
		{
			System.out.println("Error: can not play the audio");
			err.printStackTrace();
		}
	}
}

class audio extends wav
{
	public audio(String path)
	{
		try
		{
			if(new File(path+".wav").exists())
			{
				new File(path+".wav").delete();
			}
			Process p=Runtime.getRuntime().exec("ffmpeg.exe -i \""+path+"\" -f wav \""+path+".wav\"");
			try
			{
				p.waitFor();
			}
			catch(Exception err)
			{
			}
		}
		catch(IOException err)
		{
			err.printStackTrace();
		}
		this.path=path+".wav";
	}
}

class getlength
{
	public getlength()
	{
	}
	public static int get(String path)
	{
		audio au=new audio(path);
		au.pass=true;
		au.set();
		au.start();
		while(au.stop!=true)
		{
			System.out.println(au.played);
		}
		return au.played;
	}
	public static int getWavLength(String path)
	{
		wav au=new wav();
		au.path=path;
		au.pass=true;
		au.set();
		au.start();
		while(au.stop!=true)
		{
			System.out.print("");
		}
		return au.played;
	}
}

class file_system
{
	public file_system()
	{
	}
	public static void del(String path)
	{
		File file=new File(path);
		file.delete();
	}
	public static boolean copy(String from, String to)
	{
		try
		{
			File from_file=new File(from);
			File to_file=new File(to);
			if(from_file.exists())
			{
				FileInputStream sfrom=new FileInputStream(from_file);
				FileOutputStream sto=new FileOutputStream(to_file);
				byte[] bytes=new byte[1024];
				while(sfrom.read(bytes)!=-1)
				{
					sto.write(bytes);
				}
				sfrom.close();
				sto.close();
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception err)
		{
			return false;
		}
	}
}

class frame extends Thread
{
	public JFrame root=new JFrame("Music player");
	public JSplitPane panel=new JSplitPane();
	public JPanel sidebar=new JPanel(new FlowLayout());
	public JProgressBar progress_loading=new JProgressBar();
	public JPanel main=new JPanel(new FlowLayout());
	public JProgressBar progress=new JProgressBar();
	
	public ArrayList<String> list=new ArrayList<String>();
	public DefaultMutableTreeNode list_root=new DefaultMutableTreeNode("All");
	public JTree list_root_top=null;
	public ArrayList<DefaultMutableTreeNode> list_root_list=new ArrayList<DefaultMutableTreeNode>();
	public JScrollPane pic=null;
	public JLabel pic_show=new JLabel();
	public TreePath[] selection=null;
	
	public JButton pause_button=new JButton();
	public JButton next_button=new JButton();
	public JButton back_button=new JButton();
	public JButton set_button=new JButton();
	public JButton pass_button=new JButton();
	public JButton bk_button=new JButton();
	public JButton play_button=new JButton();
	public JLabel title=new JLabel();
	
	public audio au=null;
	public int files_doing=0;
	public ArrayList<String> files_list=new ArrayList<String>();
	public String method="repeat";
	public boolean first_play=true;
	public boolean forced_stop=false;
	public int maxvalue=0;
	public String album="";
	public frame()
	{
	}
	public String[] getPath(String tree_path)
	{
		tree_path=tree_path.substring(1, tree_path.length()-1);
		return tree_path.split(", ");
	}
	public void run()
	{
		root.setSize(500, 500);
		root.setPreferredSize(new Dimension(500, 500));
		
		sidebar.setBorder(BorderFactory.createTitledBorder(""));
		sidebar.setSize(new Dimension(root.getWidth()/3*1, root.getHeight()));
		sidebar.add(new JLabel("Loading..."));
		progress_loading.setMinimum(0);
		progress_loading.setMaximum(100);
		progress_loading.setValue(0);
		progress_loading.setStringPainted(true);
		progress_loading.setPreferredSize(new Dimension(sidebar.getWidth()-5, 25));
		sidebar.add(progress_loading);
		panel.setLeftComponent(sidebar);
		
		main.setSize(new Dimension(root.getWidth()/3*2, root.getHeight()));
		pic=new JScrollPane(pic_show);
		pic.setPreferredSize(new Dimension(main.getWidth()-50, main.getWidth()-50));
		main.add(pic);
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setValue(0);
		progress.setPreferredSize(new Dimension(main.getWidth()-50, 5));
		main.add(progress);
		back_button.setIcon(new ImageIcon("back.png"));
		back_button.setPreferredSize(new Dimension(35, 35));
		back_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> play=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						System.out.println("Button back");
						next_button.setEnabled(false);
						back_button.setEnabled(false);
						pass_button.setEnabled(false);
						bk_button.setEnabled(false);
						play_button.setEnabled(false);
						try
						{
							forced_stop=true;
							au.pause=false;
							au.stop=true;
							Thread.sleep(500);
						}
						catch(Exception err)
						{
							err.printStackTrace();
						}
						catch(Error err)
						{
							err.printStackTrace();
						}
						while(forced_stop!=false)
						{
							System.out.print("");
						}
						next_button.setEnabled(true);
						back_button.setEnabled(true);
						pass_button.setEnabled(true);
						bk_button.setEnabled(true);
						play_button.setEnabled(true);
						back();
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				play.execute();
			}
				});
        main.add(back_button);
        //System.out.println(System.getProperty("PATH"));
		pause_button.setIcon(new ImageIcon("pause.png"));
		pause_button.setPreferredSize(new Dimension(35, 35));
		pause_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				System.out.println("Button play");
				try
				{
					if(first_play==true)
					{
						SwingWorker<String, Object> play=new SwingWorker<String, Object>(){
							@Override
							protected String doInBackground() throws Exception
							{
								next_button.setEnabled(false);
								back_button.setEnabled(false);
								pass_button.setEnabled(false);
								bk_button.setEnabled(false);
								play_button.setEnabled(false);
								while(forced_stop!=false)
								{
									System.out.print("");
								}
								next_button.setEnabled(true);
								back_button.setEnabled(true);
								pass_button.setEnabled(true);
								bk_button.setEnabled(true);
								play_button.setEnabled(true);
								playAudio();
								return null;
							}
							@Override
							protected void done()
							{
							}
						};
						play.execute();
					}
					else
					{
						if(au.pause==true)
						{
							au.pause=false;
						}
						else
						{
							au.pause=true;
						}
					}
				}
				catch(Exception err)
				{
				}
				catch(Error err)
				{
					System.out.println("Error");
				}
			}
				});
		main.add(pause_button);
		next_button.setIcon(new ImageIcon("next.png"));
		next_button.setPreferredSize(new Dimension(35, 35));
		next_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> play=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						System.out.println("Button next");
						next_button.setEnabled(false);
						back_button.setEnabled(false);
						pass_button.setEnabled(false);
						bk_button.setEnabled(false);
						play_button.setEnabled(false);
						try
						{
							forced_stop=true;
							au.pause=false;
							au.stop=true;
							Thread.sleep(500);
						}
						catch(Exception err)
						{
							err.printStackTrace();
						}
						catch(Error err)
						{
							err.printStackTrace();
						}
						while(forced_stop!=false)
						{
							System.out.print("");
						}
						next_button.setEnabled(true);
						back_button.setEnabled(true);
						pass_button.setEnabled(true);
						bk_button.setEnabled(true);
						play_button.setEnabled(true);
						next();
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				play.execute();
			}
				});
		main.add(next_button);
		pass_button.setIcon(new ImageIcon("pass.png"));
		pass_button.setPreferredSize(new Dimension(35, 35));
		pass_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> playfrom=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						int from=au.played;
						try
						{
							forced_stop=true;
							au.pause=false;
							au.stop=true;
						}
						catch(Exception err)
						{
						}
						catch(Error err)
						{
						}
						if(files_list.size()>0)
						{
							next_button.setEnabled(false);
							back_button.setEnabled(false);
							pass_button.setEnabled(false);
							bk_button.setEnabled(false);
							play_button.setEnabled(false);
							while(forced_stop!=false)
							{
								System.out.print("");
							}
							next_button.setEnabled(true);
							back_button.setEnabled(true);
							pass_button.setEnabled(true);
							bk_button.setEnabled(true);
							play_button.setEnabled(true);
							first_play=false;
							System.out.println("Button pass");
							playAudioFrom(from+5000);
						}
						else
						{
							JOptionPane.showMessageDialog(null, "Please select a play list", "Caution", JOptionPane.PLAIN_MESSAGE);
						}
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				playfrom.execute();
			}
				});
		main.add(pass_button);
		bk_button.setIcon(new ImageIcon("bk.png"));
		bk_button.setPreferredSize(new Dimension(35, 35));
		bk_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> playfrombk=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						int from=au.played;
						try
						{
							forced_stop=true;
							au.pause=false;
							au.stop=true;
						}
						catch(Exception err)
						{
						}
						catch(Error err)
						{
						}
						if(files_list.size()>0)
						{
							next_button.setEnabled(false);
							back_button.setEnabled(false);
							pass_button.setEnabled(false);
							bk_button.setEnabled(false);
							play_button.setEnabled(false);
							while(forced_stop!=false)
							{
								System.out.print("");
							}
							next_button.setEnabled(true);
							back_button.setEnabled(true);
							pass_button.setEnabled(true);
							bk_button.setEnabled(true);
							play_button.setEnabled(true);
							first_play=false;
							from=from-5000;
							if(from<0)
							{
								from=0;
							}
							System.out.println("Button bk");
							playAudioFrom(from);
						}
						else
						{
							JOptionPane.showMessageDialog(null, "Please select a play list", "Caution", JOptionPane.PLAIN_MESSAGE);
						}
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				playfrombk.execute();
			}
				});
		main.add(bk_button);
		set_button.setText("Set playlist");
		set_button.setPreferredSize(new Dimension(100, 35));
		set_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				System.out.println("Button setPlayList");
				try
				{
					try
					{
						if(au!=null && au.stop!=true)
						{
							forced_stop=true;
						}
						au.pause=false;
						au.stop=true;
						Thread.sleep(500);
					}
					catch(Exception err)
					{
					}
					catch(Error err)
					{
					}
					next_button.setEnabled(false);
					back_button.setEnabled(false);
					pass_button.setEnabled(false);
					bk_button.setEnabled(false);
					play_button.setEnabled(false);
					progress.setValue(0);
					pic_show.setIcon(null);
					while(forced_stop!=false)
					{
						System.out.print("");
					}
					next_button.setEnabled(true);
					back_button.setEnabled(true);
					pass_button.setEnabled(true);
					bk_button.setEnabled(true);
					play_button.setEnabled(true);
					setPlayList(getPath(selection[0].toString())[1]);
				}
				catch(Exception err)
				{
					JOptionPane.showMessageDialog(null, "Error, maybe the list is still loading", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
				});
		main.add(set_button);
		JButton repeat_button=new JButton();
		repeat_button.setText("repeat all");
		repeat_button.setPreferredSize(new Dimension(135, 32));
		repeat_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				method="repeat";
				JOptionPane.showMessageDialog(null, "Method: repeat all");
			}
				});
		main.add(repeat_button);
		JButton repeat_one_button=new JButton();
		repeat_one_button.setText("repeat one");
		repeat_one_button.setPreferredSize(new Dimension(135, 32));
		repeat_one_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				method="repeat one";
				JOptionPane.showMessageDialog(null, "Method: repeat one");
			}
				});
		main.add(repeat_one_button);
		play_button.setText("play from here");
		play_button.setPreferredSize(new Dimension(135, 32));
		play_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> play=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						System.out.println("PlayAudioFrom");
						try
						{
							if(files_list.indexOf(getPath(selection[0].toString())[2])!=-1)
							{
								try
								{
									forced_stop=true;
									au.pause=false;
									au.stop=true;
									Thread.sleep(500);
								}
								catch(Exception err)
								{
									err.printStackTrace();
								}
								catch(Error err)
								{
									err.printStackTrace();
								}
								next_button.setEnabled(false);
								back_button.setEnabled(false);
								pass_button.setEnabled(false);
								bk_button.setEnabled(false);
								play_button.setEnabled(false);
								while(forced_stop!=false)
								{
									System.out.print("");
								}
								next_button.setEnabled(true);
								back_button.setEnabled(true);
								pass_button.setEnabled(true);
								bk_button.setEnabled(true);
								play_button.setEnabled(true);
								files_doing=files_list.indexOf(getPath(selection[0].toString())[2]);
								playAudio();
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Error");
							}
						}
						catch(Exception err)
						{
							JOptionPane.showMessageDialog(null, "Error: please choose a file");
						}
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				play.execute();
			}
				});
		main.add(play_button);
		JButton load_button=new JButton();
		load_button.setText("Refresh menu");
		load_button.setPreferredSize(new Dimension(135, 32));
		load_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				System.out.println("load menu");
				SwingWorker<String, Object> load=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						load_menu();
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				load.execute();
			}
				});
		main.add(load_button);
		title.setText("");
		title.setPreferredSize(new Dimension(135, 32));
		main.add(title);
		JButton mgr_button=new JButton();
		mgr_button.setText("path manager");
		mgr_button.setPreferredSize(new Dimension(135, 32));
		mgr_button.addActionListener(new ActionListener()
				{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SwingWorker<String, Object> loadmgr=new SwingWorker<String, Object>(){
					@Override
					protected String doInBackground() throws Exception
					{
						Runtime.getRuntime().exec("cmd /c start /b path_manager.exe");
						return null;
					}
					@Override
					protected void done()
					{
					}
				};
				loadmgr.execute();
			}
				});
		main.add(mgr_button);
		
		panel.setRightComponent(main);
		panel.setOneTouchExpandable(true);
		panel.setDividerLocation(root.getWidth()/3*1);
		
		root.add(panel);
		root.pack();
		root.addWindowListener(new exit());
		root.setVisible(true);
		root.setResizable(false);
		root.setAlwaysOnTop(false);
		SwingWorker<String, Object> refresh=new SwingWorker<String, Object>(){
			@Override
			protected String doInBackground() throws Exception
			{
				load_menu();
				return null;
			}
			@Override
			protected void done()
			{
			}
		};
		refresh.execute();
	}
	public void load_menu()
	{
		if(sidebar!=null)
		{
			list_root.removeAllChildren();
			list.clear();
			list_root=new DefaultMutableTreeNode("All");
			list_root_top=null;
			list_root_list.clear();
			String name;
			try
			{
				BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream("player.list")));
				int doing=0;
				while((name=reader.readLine())!=null)
				{
					list.add(doing, name);
					doing+=1;
				}
				reader.close();
				doing=0;
				while(doing<list.size())
				{
					progress_loading.setValue(Math.round(100/list.size()*doing));
					sidebar.repaint();
					Thread.sleep(1000);
					if(list.get(doing)==null)
					{
						break;
					}
					if(!(list.get(doing).indexOf(", ")==-1))
					{
						System.out.println("Error");
						System.exit(0);
					}
					File file=new File("files/"+list.get(doing).replace(", ", ""));
					String[] audio_files=file.list();
					if(!file.exists())
					{
						list_root.add(new DefaultMutableTreeNode(file.toString().substring(6)));
						doing+=1;
						continue;
					}
					int did=0;
					list_root_list.add(doing, new DefaultMutableTreeNode(file.toString().substring(6)));
					while((audio_files!=null)&&(did<audio_files.length))
					{
						list_root_list.get(doing).add(new DefaultMutableTreeNode(audio_files[did]));
						did+=1;
					}
					list_root.add(list_root_list.get(doing));
					doing+=1;
				}
			}
			catch(Exception err)
			{
				err.printStackTrace();
			}
			progress_loading.setValue(100);
			sidebar.repaint();
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception err)
			{
			}
			sidebar.removeAll();
			list_root_top=new JTree(list_root);
			list_root_top.addTreeSelectionListener(new TreeSelectionListener()
					{
				@Override
				public void valueChanged(TreeSelectionEvent event)
				{
					selection=list_root_top.getSelectionPaths();
				}
					});
			JScrollPane tree=new JScrollPane(list_root_top);
			tree.setPreferredSize(new Dimension(sidebar.getWidth()-5, sidebar.getHeight()-50));
			list_root_top.setSelectionPath(list_root_top.getPathForRow(1));
			tree.repaint();
			sidebar.add(tree);
			tree.repaint();
			sidebar.repaint();
			panel.setLeftComponent(null);
			panel.setLeftComponent(sidebar);
			panel.setDividerLocation(root.getWidth()/3*1);
		}
	}
	public void next()
	{
		next_button.setEnabled(false);
		back_button.setEnabled(false);
		pass_button.setEnabled(false);
		bk_button.setEnabled(false);
		if(!method.equals("repeat one"))
		{
			files_doing+=1;
		}
		if(files_doing+1>files_list.size())
		{
			if(method.equals("repeat"))
			{
				files_doing=0;
			}
			else if(method.equals("repeat one"))
			{
			}
			else
			{
				files_doing-=1;
			}
		}
		next_button.setEnabled(true);
		back_button.setEnabled(true);
		pass_button.setEnabled(true);
		bk_button.setEnabled(true);
		if(files_list.size()>0)
		{
			first_play=false;
			playAudio();
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Please select a play list");
		}
	}
	public void back()
	{
		next_button.setEnabled(false);
		back_button.setEnabled(false);
		pass_button.setEnabled(false);
		bk_button.setEnabled(false);
		play_button.setEnabled(false);
		if(!method.equals("repeat one"))
		{
			files_doing-=1;
		}
		if(files_doing==-1)
		{
			if(method.equals("repeat"))
			{
				files_doing=files_list.size()-1;
			}
			else if(method.equals("repeat one"))
			{
				System.out.println("repeat one");
			}
			else
			{
				files_doing+=1;
				System.out.println("add one");
			}
		}
		next_button.setEnabled(true);
		back_button.setEnabled(true);
		pass_button.setEnabled(true);
		bk_button.setEnabled(true);
		play_button.setEnabled(true);
		if(files_list.size()>0)
		{
			first_play=false;
			playAudio();
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Please select a play list");
		}
	}
	public void setPlayList(String name)
	{
		try
		{
			forced_stop=true;
			au.pause=false;
			au.stop=true;
			Thread.sleep(500);
		}
		catch(Exception err)
		{
		}
		catch(Error err)
		{
		}
		album=name;
		name="files/"+name;
		File file=new File(name);
		files_list.clear();
		if(file.exists())
		{
			String[] files=file.list();
			int doing=0;
			while(doing<files.length)
			{
				files_list.add(files[doing]);
				doing+=1;
			}
			if(files_list.size()==0)
			{
				JOptionPane.showMessageDialog(null, "The list is null");
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Playlist: "+name);
			}
			files_doing=0;
			first_play=true;
			forced_stop=false;
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Error", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void playAudio()
	{
		try
		{
			au.pause=false;
			au.stop=true;
			Thread.sleep(1500);
		}
		catch(Exception err)
		{
		}
		catch(Error err)
		{
		}
		au=null;
		if((files_list.size()>=files_doing+1)&&(forced_stop==false))
		{
			String base_name=files_list.get(files_doing);
			String name;
			name="files/"+album+"/"+base_name;
			file_system.copy(name, "memory/"+base_name);
			au=new audio("memory/"+base_name);
			au.set();
			au.start();
			System.out.println("start playAudio");
			first_play=false;
			next_button.setEnabled(false);
			back_button.setEnabled(false);
			pass_button.setEnabled(false);
			bk_button.setEnabled(false);
			play_button.setEnabled(false);
			title.setText(base_name);
			try
			{
				pic_show.setIcon(new ImageIcon("load.gif"));
				pic_show.repaint();
			}
			catch(Exception err)
			{
			}
			try
			{
				progress.setMinimum(0);
				maxvalue=getlength.getWavLength("memory/"+base_name+".wav");
				progress.setMaximum(maxvalue);
				progress.setValue(0);
				progress.repaint();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			try
			{
				pic_show.setIcon(null);
				pic_show.setIcon(new ImageIcon(ImageIO.read(new File("files/cover/"+album+"-cover.png"))));
				pic.repaint();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			next_button.setEnabled(true);
			back_button.setEnabled(true);
			pass_button.setEnabled(true);
			bk_button.setEnabled(true);
			play_button.setEnabled(true);
			while((au.stop==false)&&(forced_stop==false))
			{
				progress.setValue(au.played);
				progress.repaint();
				System.out.print("");
			}
			au.stop=true;
			au=null;
			System.out.println("Stop playAudio");
			try
			{
				Thread.sleep(1500);
			}
			catch(Exception err)
			{
			}
			try
			{
				new File("memory/"+base_name).delete();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			try
			{
				new File("memory/"+base_name+".wav").delete();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			if(forced_stop==false)
			{
				try
				{
					au.pause=false;
					au.stop=true;
					Thread.sleep(500);
				}
				catch(Exception err)
				{
				}
				catch(Error err)
				{
				}
				if(!method.equals("repeat one"))
				{
					files_doing+=1;
				}
				if(files_doing+1>files_list.size())
				{
					if(method.equals("repeat"))
					{
						files_doing=0;
					}
					else if(method.equals("repeat one"))
					{
					}
					else
					{
						files_doing-=1;
					}
				}
				if(files_list.size()>0)
				{
					first_play=false;
					playAudio();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Please select a play list");
				}
			}
			else
			{
				System.out.println("forced stop playAudio");
				forced_stop=false;
			}
		}
		else if(forced_stop==true)
		{
			forced_stop=false;
		}
		else
		{
			JOptionPane.showMessageDialog(null, "No audio is available");
		}
	}
	public void playAudioFrom(int from)
	{
		try
		{
			au.pause=false;
			au.stop=true;
		}
		catch(Exception err)
		{
		}
		catch(Error err)
		{
		}
		au=null;
		if((files_list.size()>=files_doing+1)&&(forced_stop==false))
		{
			String base_name=files_list.get(files_doing);
			String name;
			name="files/"+album+"/"+base_name;
			file_system.copy(name, "memory/"+base_name);
			au=new audio("memory/"+base_name);
			au.play_from=from;
			au.set();
			au.start();
			System.out.println("start playAudioFrom");
			first_play=false;
			try
			{
				progress.setMinimum(0);
				progress.setMaximum(maxvalue);
				progress.setValue(0);
				progress.repaint();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			try
			{
				pic_show.setIcon(null);
				pic_show.setIcon(new ImageIcon(ImageIO.read(new File("files/cover/"+album+"-cover.png"))));
				pic.repaint();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			while((au.stop==false)&&(forced_stop==false))
			{
				progress.setValue(au.played);
				progress.repaint();
				System.out.print("");
			}
			au=null;
			System.out.println("Stop playAudioFrom");
			try
			{
				Thread.sleep(1500);
			}
			catch(Exception err)
			{
			}
			try
			{
				new File("memory/"+base_name).delete();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			try
			{
				new File("memory/"+base_name+".wav").delete();
			}
			catch(Exception err)
			{
			}
			catch(Error err)
			{
			}
			if(forced_stop==false)
			{
				try
				{
					au.pause=false;
					au.stop=true;
					Thread.sleep(500);
				}
				catch(Exception err)
				{
				}
				catch(Error err)
				{
				}
				if(!method.equals("repeat one"))
				{
					files_doing+=1;
				}
				if(files_doing+1>files_list.size())
				{
					if(method.equals("repeat"))
					{
						files_doing=0;
					}
					else if(method.equals("repeat one"))
					{
					}
					else
					{
						files_doing-=1;
					}
				}
				if(files_list.size()>0)
				{
					first_play=false;
					playAudio();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Please select a play list");
				}
			}
			else
			{
				forced_stop=false;
			}
		}
		else if(forced_stop==true)
		{
			forced_stop=false;
			System.out.println("hi");
		}
		else
		{
			JOptionPane.showMessageDialog(null, "No audio is available");
		}
	}
	public void setPlayListAndPlay(String folder, String name)
	{
		setPlayList(folder);
		if(files_list.indexOf(name)!=-1)
		{
			files_doing=files_list.indexOf(name);
		}
		playAudio();
	}
}

public class player extends Thread
{
	public static void main(String [] args)
	{
		try
		{
            File file=new File("memory/");
            if(!file.exists())
            {
                file.createNewFile();
            }
			String[] files=file.list();
            int doing=0;
			while(files!=null&&doing<files.length)
			{
				new File("memory/"+files[doing]).delete();
				doing+=1;
			}
			Thread.sleep(1000);
			new frame().start();
		}
		catch(Exception err)
		{
			err.printStackTrace();
		}
	}
}